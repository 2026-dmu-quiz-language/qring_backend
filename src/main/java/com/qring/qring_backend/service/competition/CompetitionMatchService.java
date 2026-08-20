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
import com.qring.qring_backend.domain.competition.CompetitionMatchAnswer;
import com.qring.qring_backend.domain.competition.CompetitionMatchAnswerRepository;
import com.qring.qring_backend.domain.competition.CompetitionMatchRepository;
import com.qring.qring_backend.domain.competition.CompetitionQuizContent;
import com.qring.qring_backend.domain.competition.CompetitionQuizContentRepository;
import com.qring.qring_backend.domain.competition.CompetitionWrongAnswer;
import com.qring.qring_backend.domain.competition.CompetitionWrongAnswerRepository;
import com.qring.qring_backend.domain.quiz.QuizContentRepository;
import com.qring.qring_backend.domain.quiz.QuizDetail;
import com.qring.qring_backend.domain.quiz.QuizDetailRepository;
import com.qring.qring_backend.domain.quiz.QuizContent;
import com.qring.qring_backend.domain.quiz.WrongAnswer;
import com.qring.qring_backend.domain.quiz.WrongAnswerRepository;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.domain.user.UserAsset;
import com.qring.qring_backend.domain.user.UserAssetHistory;
import com.qring.qring_backend.domain.user.UserAssetHistory.SourceType;
import com.qring.qring_backend.domain.user.UserAssetHistoryRepository;
import com.qring.qring_backend.domain.user.UserAssetRepository;
import com.qring.qring_backend.dto.competition.BotLevelDto;
import com.qring.qring_backend.dto.competition.BotResultDto;
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

    // 레벨별 기본 보상 (상/중/하)
    private static final Map<Integer, Integer> BASE_REWARD = Map.of(1, 100, 2, 140, 3, 200);

    private final UserRepository userRepository;
    private final UserAssetRepository userAssetRepository;
    private final UserAssetHistoryRepository userAssetHistoryRepository;
    private final CompetitionMatchRepository competitionMatchRepository;
    private final CompetitionMatchAnswerRepository competitionMatchAnswerRepository;
    private final CompetitionQuizContentRepository competitionQuizContentRepository;
    private final CompetitionBotProfileRepository competitionBotProfileRepository;
    private final CompetitionWrongAnswerRepository competitionWrongAnswerRepository;
    private final QuizDetailRepository quizDetailRepository;
    private final QuizContentRepository quizContentRepository;
    private final WrongAnswerRepository wrongAnswerRepository;

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

    /**
     * 매치 일시정지/재개 토글. 유저당 진행 중인 매치는 1개라고 보고 status로 찾음.
     * 일시정지는 포인트 변동 없음 (결정사항).
     */
    @Transactional
    public CompetitionMatch togglePause(Long userId, boolean pause) {
        List<CompetitionMatch> activeMatches = competitionMatchRepository.findAllByUserIdAndStatusIn(
                userId,
                List.of(CompetitionMatch.MatchStatus.IN_PROGRESS, CompetitionMatch.MatchStatus.PAUSED));

        if (activeMatches.isEmpty()) {
            throw new IllegalArgumentException("진행 중인 매치가 없습니다.");
        }

        CompetitionMatch match = activeMatches.get(0);

        if (pause && match.getStatus() == CompetitionMatch.MatchStatus.IN_PROGRESS) {
            match.setStatus(CompetitionMatch.MatchStatus.PAUSED);
            match.setPausedAt(LocalDateTime.now());
        } else if (!pause && match.getStatus() == CompetitionMatch.MatchStatus.PAUSED) {
            match.setStatus(CompetitionMatch.MatchStatus.IN_PROGRESS);
            match.setPausedAt(null);
        }
        // 이미 같은 상태면 그대로 idempotent 처리

        return competitionMatchRepository.save(match);
    }

    /**
     * 매치 결과 저장 + 점수/포인트 계산.
     * 프론트가 판정한 정답 여부를 그대로 신뢰하고 저장 (재검증 없음).
     */
    @Transactional
    public BotResultDto.Response saveResult(Long userId, BotResultDto.Request request) {

        List<CompetitionMatch> activeMatches = competitionMatchRepository.findAllByUserIdAndStatusIn(
                userId,
                List.of(CompetitionMatch.MatchStatus.IN_PROGRESS, CompetitionMatch.MatchStatus.PAUSED));

        if (activeMatches.isEmpty()) {
            throw new IllegalArgumentException("진행 중인 매치가 없습니다.");
        }
        CompetitionMatch match = activeMatches.get(0);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        String langCode = user.getLanguage();

        int correctCount = 0;

        for (BotResultDto.AnswerItem item : request.getAnswers()) {

            String roundWinner;
            if (item.isUserIsCorrect() && !item.isBotIsCorrect()) {
                roundWinner = "USER";
            } else if (!item.isUserIsCorrect() && item.isBotIsCorrect()) {
                roundWinner = "BOT";
            } else {
                roundWinner = "DRAW";
            }

            CompetitionMatchAnswer answer = new CompetitionMatchAnswer();
            answer.setMatch(match);
            answer.setSourceQuizContentId(item.getSourceQuizContentId());
            answer.setSourceType(CompetitionMatchAnswer.SourceType.valueOf(item.getSourceType()));
            answer.setRoundNo(item.getRoundNo());
            answer.setUserAnswer(item.getUserAnswer());
            answer.setUserIsCorrect(item.isUserIsCorrect());
            answer.setBotIsCorrect(item.isBotIsCorrect());
            answer.setRoundWinner(CompetitionMatchAnswer.RoundWinner.valueOf(roundWinner));
            competitionMatchAnswerRepository.save(answer);

            if (item.isUserIsCorrect()) {
                correctCount++;
                continue;
            }

            // 오답 저장 (출처별로 다른 테이블)
            if ("STORY".equals(item.getSourceType())) {
                saveStoryWrongAnswer(userId, item.getSourceQuizContentId());
            } else {
                saveCompetitionWrongAnswer(userId, item.getSourceQuizContentId(), match.getLevel(), langCode);
            }
        }

        int wrongCount = request.getAnswers().size() - correctCount;

        // 보상 계산: 기본 보상 + 스트릭 보너스 (연속구간마다 계산해서 합산 - 옵션 B)
        int baseReward = BASE_REWARD.getOrDefault(match.getLevel(), 0);
        int streakBonus = calculateStreakBonusFromAnswers(request.getAnswers());
        int rewardPoint = baseReward + streakBonus;

        match.setStatus(CompetitionMatch.MatchStatus.COMPLETED);
        match.setCorrectCount(correctCount);
        match.setRewardPoint(rewardPoint);
        match.setCompletedAt(LocalDateTime.now());
        competitionMatchRepository.save(match);

        // 보상 지급 + 이력 기록
        UserAsset asset = userAssetRepository.findByUserUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 자산 정보를 찾을 수 없습니다."));
        userAssetRepository.addPoints(userId, rewardPoint);
        int balanceAfter = asset.getCurrentPoints() + rewardPoint;

        UserAssetHistory history = new UserAssetHistory();
        history.setUserId(userId);
        history.setChangeAmount(rewardPoint);
        history.setBalanceAfter(balanceAfter);
        history.setSourceType(SourceType.COMPETITION_REWARD);
        history.setReferenceId(match.getMatchId());
        userAssetHistoryRepository.save(history);

        return new BotResultDto.Response(match.getMatchId(), correctCount, wrongCount, rewardPoint, balanceAfter);
    }

    /**
     * 21문제(roundNo 순서)를 훑어서 끊기지 않는 연속 정답 구간(run)을 찾고,
     * 구간마다 도달한 최고 티어 보너스를 각각 계산해서 합산.
     * 예: 7연속 -> 오답 -> 7연속 이면 +10 + +10 = +20
     */
    private int calculateStreakBonusFromAnswers(List<BotResultDto.AnswerItem> answers) {
        List<BotResultDto.AnswerItem> sorted = answers.stream()
                .sorted((a, b) -> Integer.compare(a.getRoundNo(), b.getRoundNo()))
                .collect(Collectors.toList());

        int totalBonus = 0;
        int currentRun = 0;

        for (BotResultDto.AnswerItem item : sorted) {
            if (item.isUserIsCorrect()) {
                currentRun++;
            } else {
                totalBonus += streakTierBonus(currentRun);
                currentRun = 0;
            }
        }
        totalBonus += streakTierBonus(currentRun); // 마지막 구간 처리

        return totalBonus;
    }

    private int streakTierBonus(int runLength) {
        if (runLength >= TOTAL_QUESTIONS) return 35; // 21문제 올백
        if (runLength >= 14) return 25;
        if (runLength >= 7) return 10;
        return 0;
    }

    private void saveStoryWrongAnswer(Long userId, Long quizContentId) {
        boolean alreadyExists = wrongAnswerRepository.findByUserIdAndQuizContentId(userId, quizContentId).isPresent();
        if (alreadyExists) return;

        QuizContent quizContent = quizContentRepository.findById(quizContentId)
                .orElseThrow(() -> new IllegalArgumentException("스토리 문제를 찾을 수 없습니다: " + quizContentId));
        QuizDetail quizDetail = quizContent.getQuizDetail();

        WrongAnswer wa = new WrongAnswer();
        wa.setUserId(userId);
        wa.setQuizContentId(quizContentId);
        wa.setLevel(quizDetail.getDifficulty());
        wa.setStoryName(quizDetail.getContent().getTitle());
        wa.setContentId(quizDetail.getContent().getContentId());
        wrongAnswerRepository.save(wa);
    }

    private void saveCompetitionWrongAnswer(Long userId, Long quizContentId, Integer level, String langCode) {
        boolean alreadyExists = competitionWrongAnswerRepository
                .findByUserIdAndQuizContentId(userId, quizContentId).isPresent();
        if (alreadyExists) return;

        CompetitionQuizContent quizContent = competitionQuizContentRepository.findById(quizContentId)
                .orElseThrow(() -> new IllegalArgumentException("컴피티션 문제를 찾을 수 없습니다: " + quizContentId));

        CompetitionWrongAnswer wa = new CompetitionWrongAnswer();
        wa.setUserId(userId);
        wa.setQuizContent(quizContent);
        wa.setLevel(level);
        wa.setLangCode(langCode);
        competitionWrongAnswerRepository.save(wa);
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