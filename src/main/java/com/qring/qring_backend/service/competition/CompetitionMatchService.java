package com.qring.qring_backend.service.competition;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.domain.competition.CompetitionBotProfile;
import com.qring.qring_backend.domain.competition.CompetitionBotProfileRepository;
import com.qring.qring_backend.domain.competition.CompetitionMatch;
import com.qring.qring_backend.domain.competition.CompetitionMatchRepository;
import com.qring.qring_backend.domain.competition.CompetitionQuizContent;
import com.qring.qring_backend.domain.competition.CompetitionQuizContentRepository;
import com.qring.qring_backend.domain.quiz.QuizContentRepository;
import com.qring.qring_backend.domain.quiz.QuizDetail;
import com.qring.qring_backend.domain.quiz.QuizDetailRepository;
import com.qring.qring_backend.domain.quiz.QuizContent;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.domain.user.UserAsset;
import com.qring.qring_backend.domain.user.UserAssetHistory;
import com.qring.qring_backend.domain.user.UserAssetHistory.SourceType;
import com.qring.qring_backend.domain.user.UserAssetHistoryRepository;
import com.qring.qring_backend.domain.user.UserAssetRepository;
import com.qring.qring_backend.dto.competition.BotLevelDto;
import com.qring.qring_backend.dto.competition.CompetitionQuizItemDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompetitionMatchService {

    private static final int TOTAL_QUESTIONS = 21;
    private static final int QUESTIONS_PER_TYPE = 7;
    private static final int STORY_QUESTION_COUNT = 4;

    private final UserRepository userRepository;
    private final UserAssetRepository userAssetRepository;
    private final UserAssetHistoryRepository userAssetHistoryRepository;
    private final CompetitionMatchRepository competitionMatchRepository;
    private final CompetitionQuizContentRepository competitionQuizContentRepository;
    private final CompetitionBotProfileRepository competitionBotProfileRepository;
    private final QuizDetailRepository quizDetailRepository;
    private final QuizContentRepository quizContentRepository;

    @Transactional
    public BotLevelDto.Response startMatch(Long userId, BotLevelDto.Request request) {

        int level = mapBotLevelToInt(request.getBotLevel());
        int entryCost = request.getEntryCost();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        String langCode = user.getLanguage();

        UserAsset asset = userAssetRepository.findByUserUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 자산 정보를 찾을 수 없습니다."));

        if (asset.getCurrentPoints() < entryCost) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        // 1. 문제 선정: 스토리 4문제 + 신규 17문제 (유형별 정확히 7개씩)
        List<CompetitionQuizItemDto> questions = selectQuestions(level, langCode);

        // 2. 매치 row 생성
        CompetitionMatch match = new CompetitionMatch();
        match.setUserId(userId);
        match.setLevel(level);
        match.setStatus(CompetitionMatch.MatchStatus.IN_PROGRESS);
        match.setEntryCost(entryCost);
        match.setCorrectCount(0);
        match.setTotalCount(TOTAL_QUESTIONS);
        match.setStartedAt(LocalDateTime.now());
        competitionMatchRepository.save(match);

        // 3. entry_cost 차감 + 이력 기록
        userAssetRepository.addPoints(userId, -entryCost);
        int balanceAfter = asset.getCurrentPoints() - entryCost;

        UserAssetHistory history = new UserAssetHistory();
        history.setUserId(userId);
        history.setChangeAmount(-entryCost);
        history.setBalanceAfter(balanceAfter);
        history.setSourceType(SourceType.COMPETITION_ENTRY);
        history.setReferenceId(match.getMatchId());
        userAssetHistoryRepository.save(history);

        return new BotLevelDto.Response(match.getMatchId(), questions, balanceAfter);
    }

    private int mapBotLevelToInt(String botLevel) {
        return switch (botLevel) {
            case "하" -> 1;
            case "중" -> 2;
            case "상" -> 3;
            default -> throw new IllegalArgumentException("잘못된 botLevel 값입니다: " + botLevel);
        };
    }

    private List<CompetitionQuizItemDto> selectQuestions(int level, String langCode) {

        // 1) 스토리 문제 4개 랜덤 추출 (유형 무관, 난이도만 매칭)
        List<QuizDetail> storyPool = quizDetailRepository.findAllByDifficulty(level);
        Collections.shuffle(storyPool);

        List<QuizContent> storyPicked = new ArrayList<>();
        for (QuizDetail qd : storyPool) {
            if (storyPicked.size() >= STORY_QUESTION_COUNT) break;
            quizContentRepository.findByQuizIdAndLangCode(qd.getQuizId(), langCode)
                    .ifPresent(storyPicked::add);
        }

        // 2) 스토리에서 뽑힌 문제의 유형별 개수 집계 (fill_in_blank -> subjective로 매핑)
        Map<String, Long> storyTypeCounts = storyPicked.stream()
                .collect(Collectors.groupingBy(
                        qc -> normalizeType(qc.getQuizDetail().getQuizType()),
                        Collectors.counting()));

        int mcNeeded = QUESTIONS_PER_TYPE - storyTypeCounts.getOrDefault("multiple_choice", 0L).intValue();
        int subjNeeded = QUESTIONS_PER_TYPE - storyTypeCounts.getOrDefault("subjective", 0L).intValue();
        int wordNeeded = QUESTIONS_PER_TYPE; // 스토리에 word_arrange 없음, 항상 신규에서 전부

        // 3) 신규(컴피티션) 문제 풀에서 유형별로 부족한 만큼 랜덤 추출
        List<CompetitionQuizContent> compPool = competitionQuizContentRepository
                .findAllByLevelAndLangCode(level, langCode);

        List<CompetitionQuizContent> mcPicked = pickByType(compPool, "multiple_choice", mcNeeded);
        List<CompetitionQuizContent> subjPicked = pickByType(compPool, "subjective", subjNeeded);
        List<CompetitionQuizContent> wordPicked = pickByType(compPool, "word_arrange", wordNeeded);

        // 4) 봇 정답률 프로필 조회
        int correctRate = competitionBotProfileRepository.findById(level)
                .map(CompetitionBotProfile::getCorrectRate)
                .orElse(50); // 프로필 없으면 기본 50%

        // 5) 최종 문제 리스트 조립 + 봇 시뮬레이션
        List<CompetitionQuizItemDto> result = new ArrayList<>();
        for (QuizContent qc : storyPicked) {
            result.add(toDto("STORY", qc, correctRate));
        }
        for (CompetitionQuizContent qc : mcPicked) {
            result.add(toDto("COMPETITION", qc, correctRate));
        }
        for (CompetitionQuizContent qc : subjPicked) {
            result.add(toDto("COMPETITION", qc, correctRate));
        }
        for (CompetitionQuizContent qc : wordPicked) {
            result.add(toDto("COMPETITION", qc, correctRate));
        }

        Collections.shuffle(result);
        return result;
    }

    private List<CompetitionQuizContent> pickByType(List<CompetitionQuizContent> pool, String type, int count) {
        List<CompetitionQuizContent> filtered = pool.stream()
                .filter(qc -> type.equals(qc.getQuizDetail().getQuizType()))
                .collect(Collectors.toList());
        Collections.shuffle(filtered);
        return filtered.stream().limit(Math.max(count, 0)).collect(Collectors.toList());
    }

    private String normalizeType(String rawType) {
        if ("fill_in_blank".equals(rawType)) {
            return "subjective";
        }
        return rawType;
    }

    private boolean simulateBotCorrect(int correctRate) {
        return ThreadLocalRandom.current().nextInt(100) < correctRate;
    }

    private CompetitionQuizItemDto toDto(String sourceType, QuizContent qc, int correctRate) {
        return new CompetitionQuizItemDto(
                sourceType,
                qc.getQuizContentId(),
                normalizeType(qc.getQuizDetail().getQuizType()),
                qc.getQuestion(),
                null,
                null,
                null,
                null,
                qc.getOptions(),
                qc.getCorrectAnswer(),
                qc.getAcceptableAnswers(),
                simulateBotCorrect(correctRate)
        );
    }

    private CompetitionQuizItemDto toDto(String sourceType, CompetitionQuizContent qc, int correctRate) {
        return new CompetitionQuizItemDto(
                sourceType,
                qc.getQuizContentId(),
                qc.getQuizDetail().getQuizType(),
                qc.getQuestion(),
                qc.getKorean(),
                qc.getTiles(),
                qc.getAnswerTiles(),
                qc.getDistractorTiles(),
                qc.getOptions(),
                qc.getAnswer(),
                qc.getAcceptableAnswers(),
                simulateBotCorrect(correctRate)
        );
    }
}