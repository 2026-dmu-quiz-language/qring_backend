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
import com.qring.qring_backend.domain.quiz.QuizContent;
import com.qring.qring_backend.domain.quiz.QuizContentRepository;
import com.qring.qring_backend.domain.quiz.QuizDetail;
import com.qring.qring_backend.domain.quiz.QuizDetailRepository;
import com.qring.qring_backend.domain.quiz.WrongAnswer;
import com.qring.qring_backend.domain.quiz.WrongAnswerRepository;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.domain.user.UserAssetHistory.SourceType;
import com.qring.qring_backend.domain.user.UserStudyLog;
import com.qring.qring_backend.domain.user.UserStudyLogRepository;
import com.qring.qring_backend.dto.competition.BotLevelDto;
import com.qring.qring_backend.dto.competition.BotResultDto;
import com.qring.qring_backend.dto.competition.CompetitionQuizItemDto;
import com.qring.qring_backend.service.user.UserPointService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompetitionMatchService {

    private static final int TOTAL_QUESTIONS = 21;
    private static final int QUESTIONS_PER_TYPE = 7;
    private static final int STORY_QUESTION_COUNT = 4;

    // 레벨별 우승 보상 (상/중/하) — 참가만으로는 지급되지 않음, 승리해야 지급
    private static final Map<Integer, Integer> WIN_REWARD = Map.of(1, 100, 2, 140, 3, 200);

    // 레벨별 입장 비용 — 반드시 서버가 결정한다.
    private static final Map<Integer, Integer> ENTRY_COST = Map.of(1, 50, 2, 70, 3, 100);

    private final UserRepository userRepository;
    private final UserPointService userPointService;
    private final CompetitionMatchRepository competitionMatchRepository;
    private final CompetitionMatchAnswerRepository competitionMatchAnswerRepository;
    private final CompetitionQuizContentRepository competitionQuizContentRepository;
    private final CompetitionBotProfileRepository competitionBotProfileRepository;
    private final CompetitionWrongAnswerRepository competitionWrongAnswerRepository;
    private final QuizDetailRepository quizDetailRepository;
    private final QuizContentRepository quizContentRepository;
    private final WrongAnswerRepository wrongAnswerRepository;
    private final UserStudyLogRepository userStudyLogRepository;

    @Transactional
    public BotLevelDto.Response startMatch(Long userId, BotLevelDto.Request request) {

        int level = mapBotLevelToInt(request.getBotLevel());
        int entryCost = ENTRY_COST.get(level);
        if (request.getEntryCost() != null && request.getEntryCost() != entryCost) {
            log.warn("[Competition] 프론트 entryCost({})와 서버 기준({}) 불일치 - 서버 값으로 차감. userId: {}",
                    request.getEntryCost(), entryCost, userId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        String langCode = user.getLanguage();

        List<CompetitionQuizItemDto> questions = selectQuestions(level, langCode);

        CompetitionMatch match = new CompetitionMatch();
        match.setUserId(userId);
        match.setLevel(level);
        match.setStatus(CompetitionMatch.MatchStatus.IN_PROGRESS);
        match.setEntryCost(entryCost);
        match.setCorrectCount(0);
        match.setTotalCount(TOTAL_QUESTIONS);
        match.setStartedAt(LocalDateTime.now());
        competitionMatchRepository.save(match);

        // 입장 비용 차감 + 히스토리 (잔액 부족이면 InsufficientPointsException → 400, 매치 insert 도 롤백)
        int balanceAfter = userPointService.spend(userId, entryCost, SourceType.COMPETITION_ENTRY, match.getMatchId());

        return new BotLevelDto.Response(match.getMatchId(), questions, balanceAfter);
    }

    /**
     * 매치 일시정지/재개 토글. matchId로 특정 매치를 지정해서 처리.
     * 일시정지는 포인트 변동 없음 (결정사항).
     */
    @Transactional
    public CompetitionMatch togglePause(Long userId, Long matchId, boolean pause) {
        CompetitionMatch match = competitionMatchRepository.findByMatchIdAndUserId(matchId, userId)
                .orElseThrow(() -> new IllegalArgumentException("매치를 찾을 수 없습니다."));

        if (match.getStatus() != CompetitionMatch.MatchStatus.IN_PROGRESS
                && match.getStatus() != CompetitionMatch.MatchStatus.PAUSED) {
            throw new IllegalArgumentException("일시정지/재개할 수 없는 매치 상태입니다.");
        }

        if (pause && match.getStatus() == CompetitionMatch.MatchStatus.IN_PROGRESS) {
            match.setStatus(CompetitionMatch.MatchStatus.PAUSED);
            match.setPausedAt(LocalDateTime.now());
        } else if (!pause && match.getStatus() == CompetitionMatch.MatchStatus.PAUSED) {
            match.setStatus(CompetitionMatch.MatchStatus.IN_PROGRESS);
            match.setPausedAt(null);
        }

        return competitionMatchRepository.save(match);
    }

    /**
     * 매치 결과 저장 + 점수/포인트 계산.
     * matchId로 매치를 특정해서 조회 (활성 매치 중 첫 번째를 가져오던 기존 버그 수정).
     * 승패 판정: 라운드 승수가 봇보다 많아야 승리. 무승부/패배는 보상 없음.
     */
    @Transactional
    public BotResultDto.Response saveResult(Long userId, BotResultDto.Request request) {

        CompetitionMatch match = competitionMatchRepository
                .findByMatchIdAndUserId(request.getMatchId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("매치를 찾을 수 없습니다."));

        if (match.getStatus() != CompetitionMatch.MatchStatus.IN_PROGRESS
                && match.getStatus() != CompetitionMatch.MatchStatus.PAUSED) {
            throw new IllegalArgumentException("이미 종료되었거나 처리할 수 없는 매치입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        String langCode = user.getLanguage();

        int correctCount = 0;
        int userRoundWins = 0;
        int botRoundWins = 0;

        for (BotResultDto.AnswerItem item : request.getAnswers()) {

            String roundWinner;
            if (item.isUserIsCorrect() && !item.isBotIsCorrect()) {
                roundWinner = "USER";
                userRoundWins++;
            } else if (!item.isUserIsCorrect() && item.isBotIsCorrect()) {
                roundWinner = "BOT";
                botRoundWins++;
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

            if ("STORY".equals(item.getSourceType())) {
                saveStoryWrongAnswer(userId, item.getSourceQuizContentId());
            } else {
                saveCompetitionWrongAnswer(userId, item.getSourceQuizContentId(), match.getLevel(), langCode);
            }
        }

        int wrongCount = request.getAnswers().size() - correctCount;

        boolean isWin = userRoundWins > botRoundWins;

        int rewardPoint;
        if (isWin) {
            int winReward = WIN_REWARD.getOrDefault(match.getLevel(), 0);
            int streakBonus = calculateStreakBonusFromAnswers(request.getAnswers());
            rewardPoint = winReward + streakBonus;
        } else {
            rewardPoint = 0;
        }

        match.setStatus(CompetitionMatch.MatchStatus.COMPLETED);
        match.setCorrectCount(correctCount);
        match.setRewardPoint(rewardPoint);
        match.setCompletedAt(LocalDateTime.now());
        competitionMatchRepository.save(match);

        UserStudyLog studyLog = new UserStudyLog();
        studyLog.setUser(user);
        studyLog.setQuiz(null);
        studyLog.setLangCode(langCode);
        userStudyLogRepository.save(studyLog);

        // 승리 보상 적립 + 히스토리. 보상이 0 이면 기록 없이 잔액만 돌려준다.
        int balanceAfter = userPointService.earn(userId, rewardPoint, SourceType.COMPETITION_REWARD, match.getMatchId());

        return new BotResultDto.Response(match.getMatchId(), correctCount, wrongCount, rewardPoint, balanceAfter);
    }

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
        totalBonus += streakTierBonus(currentRun);

        return totalBonus;
    }

    private int streakTierBonus(int runLength) {
        if (runLength >= TOTAL_QUESTIONS) return 35;
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

        List<QuizDetail> storyPool = quizDetailRepository.findAllByDifficulty(level);
        Collections.shuffle(storyPool);

        List<QuizContent> storyPicked = new ArrayList<>();
        for (QuizDetail qd : storyPool) {
            if (storyPicked.size() >= STORY_QUESTION_COUNT) break;
            quizContentRepository.findByQuizIdAndLangCode(qd.getQuizId(), langCode)
                    .ifPresent(storyPicked::add);
        }

        Map<String, Long> storyTypeCounts = storyPicked.stream()
                .collect(Collectors.groupingBy(
                        qc -> normalizeType(qc.getQuizDetail().getQuizType()),
                        Collectors.counting()));

        int mcNeeded = QUESTIONS_PER_TYPE - storyTypeCounts.getOrDefault("multiple_choice", 0L).intValue();
        int subjNeeded = QUESTIONS_PER_TYPE - storyTypeCounts.getOrDefault("subjective", 0L).intValue();
        int wordNeeded = QUESTIONS_PER_TYPE;

        List<CompetitionQuizContent> compPool = competitionQuizContentRepository
                .findAllByLevelAndLangCode(level, langCode);

        List<CompetitionQuizContent> mcPicked = pickByType(compPool, "multiple_choice", mcNeeded);
        List<CompetitionQuizContent> subjPicked = pickByType(compPool, "subjective", subjNeeded);
        List<CompetitionQuizContent> wordPicked = pickByType(compPool, "word_arrange", wordNeeded);

        int correctRate = competitionBotProfileRepository.findById(level)
                .map(CompetitionBotProfile::getCorrectRate)
                .orElse(50);

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