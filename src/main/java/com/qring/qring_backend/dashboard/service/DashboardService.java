package com.qring.qring_backend.dashboard.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.dashboard.dto.DashboardResponse;
import com.qring.qring_backend.domain.difficulty.DifficultyLevel;
import com.qring.qring_backend.domain.difficulty.DifficultyLevelRepository;
import com.qring.qring_backend.domain.quiz.AchievementCommentRepository;
import com.qring.qring_backend.domain.quiz.QuizResultRepository;
import com.qring.qring_backend.domain.quiz.StoryProgressRepository;
import com.qring.qring_backend.domain.quiz.WrongAnswerRepository;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.domain.user.UserAssetHistory.SourceType;
import com.qring.qring_backend.domain.user.UserAssetRepository;
import com.qring.qring_backend.domain.user.UserStudyLogRepository;
import com.qring.qring_backend.domain.user.UserprogressRepository;
import com.qring.qring_backend.service.user.UserPointService;

import lombok.RequiredArgsConstructor;

/** 대시보드용 통계 집계: 평균 진도율, 완료 스토리 수, 연속 학습일, 성취 코멘트, 난이도 설명. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    /** 연속 학습 보상 주기(일)와 금액. 15·30·45… 일째 대시보드 조회 시 지급. */
    static final int STREAK_REWARD_INTERVAL_DAYS = 15;
    static final int STREAK_REWARD_POINTS = 30;

    private final UserRepository userRepository;
    private final UserprogressRepository userprogressRepository;
    private final UserStudyLogRepository userStudyLogRepository;
    private final DifficultyLevelRepository difficultyLevelRepository;
    private final AchievementCommentRepository achievementCommentRepository;

    private final WrongAnswerRepository wrongAnswerRepository;
    private final UserAssetRepository userAssetRepository;
    private final UserPointService userPointService;
    private final StoryProgressRepository storyProgressRepository;
    private final QuizResultRepository quizResultRepository;

    /** 사용자별 대시보드 응답 조립. 평균 진도율은 반올림 정수, 코멘트/레벨 설명은 옵션. */
    @Transactional
    public DashboardResponse getDashboard(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
            
        String langCode = user.getLanguage();

        int progressRate = computeWeeklyProgressRate(userId);

        // 완료 스토리 수는 콘텐츠 목록의 완료 표시와 같은 기준 — 현재 언어·현재 레벨에서 완료한 콘텐츠 수.
        // 주 근거 quiz_result(난이도·언어별 풀이 기록) + 보조 근거 story_progress(마지막 완료 레벨) 의 합집합.
        long completedStoryCount = 0;
        if (langCode != null && user.getLevelCode() != null) {
            Set<Long> completed = new HashSet<>(
                    quizResultRepository.findCompletedContentIds(userId, langCode, user.getLevelCode()));
            completed.addAll(storyProgressRepository.findCompletedContentIds(userId, langCode, user.getLevelCode()));
            completedStoryCount = completed.size();
        }

        String commentText = achievementCommentRepository.findCommentByRate(progressRate).orElse(null);

        long consecutiveDays = computeConsecutiveDays(userId);

        boolean[] weeklyStudy = computeWeeklyStudy(userId);

        Integer levelCode = user.getLevelCode();
        String levelDesc = null;
        if (levelCode != null) {
            levelDesc = difficultyLevelRepository.findById(levelCode)
                .map(DifficultyLevel::getLevelDesc)
                .orElse(null);
        }

        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(7).atStartOfDay();

        String incorrectAlarm = "";
        if (langCode != null) {
            boolean hasOldIncorrect = wrongAnswerRepository.existsByUserIdAndLangCodeAndOlderThan(
                userId, langCode, sevenDaysAgo
            );
            if (hasOldIncorrect) {
                incorrectAlarm = "오답을 확인한 지 7일이 지났어요! 오답 노트를 확인해 보세요.";
            }
        }

        // 잔액·가드값은 UserAsset 엔티티가 아니라 스칼라로 읽는다. 엔티티를 들고 있다가 save 하면
        // 벌크 UPDATE 로 올린 포인트를 옛 값으로 덮어써서, 보상이 응답에는 찍히고 DB 에는 남지 않았다.
        boolean isConsecutivePointReceived = false;
        Integer currentPoints = userAssetRepository.findCurrentPointsByUserId(userId).orElse(null);
        if (currentPoints == null) {
            currentPoints = 0;
        } else if (consecutiveDays > 0 && consecutiveDays % STREAK_REWARD_INTERVAL_DAYS == 0) {
            // streak_days = 마지막으로 보상한 연속일. 같은 배수에서 재조회해도 두 번 주지 않는다.
            int lastRewardedStreak = userAssetRepository.findStreakDaysByUserId(userId).orElse(0);
            if (lastRewardedStreak < consecutiveDays) {
                currentPoints = userPointService.earn(userId, STREAK_REWARD_POINTS,
                        SourceType.STREAK_REWARD, consecutiveDays);
                userAssetRepository.updateStreakDays(userId, (int) consecutiveDays);
                isConsecutivePointReceived = true;
            }
        }

        int incorrectQuizCount = (int) wrongAnswerRepository.countByUserIdAndLangCode(userId, langCode, sevenDaysAgo);

        return DashboardResponse.builder()
            .name(user.getNickname())
            .consecutiveDays(consecutiveDays)
            .progressRate(progressRate)
            .commentText(commentText)
            .completedStoryCount(completedStoryCount)
            .levelDesc(levelDesc)
            .levelCode(levelCode)
            .weeklyStudy(weeklyStudy)
            .incorrectAlarm(incorrectAlarm)
            .isConsecutivePointReceived(isConsecutivePointReceived)
            .currentPoints(currentPoints)
            .incorrectQuizCount(incorrectQuizCount)
            .build();
    }

    /** 이번 주 성취도: 월~금 학습 시 +10%, 토~일 +25%. 매주 월요일 자동 초기화. */
    private int computeWeeklyProgressRate(Long userId) {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDateTime startOfWeek = monday.atStartOfDay();
        LocalDateTime endOfWeek = monday.plusDays(7).atStartOfDay();
        
        Set<LocalDate> studied = userStudyLogRepository.findStudyDatesBetween(userId, startOfWeek, endOfWeek)
            .stream()
            .map(LocalDateTime::toLocalDate)
            .collect(Collectors.toSet());
            
        int rate = 0;
        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);
            if (studied.contains(day)) {
                DayOfWeek dow = day.getDayOfWeek();
                rate += (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) ? 25 : 10;
            }
        }
        return rate;
    }

    /** 이번 주 월~일 학습 여부 배열. 인덱스 0=월, 6=일. */
    private boolean[] computeWeeklyStudy(Long userId) {
        boolean[] result = new boolean[7];
        
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDateTime startOfWeek = monday.atStartOfDay();
        LocalDateTime endOfWeek = monday.plusDays(7).atStartOfDay();
        
        Set<LocalDate> studied = userStudyLogRepository.findStudyDatesBetween(userId, startOfWeek, endOfWeek)
            .stream()
            .map(LocalDateTime::toLocalDate)
            .collect(Collectors.toSet());
            
        for (int i = 0; i < 7; i++) {
            result[i] = studied.contains(monday.plusDays(i));
        }
        return result;
    }

    /** 가장 최근 학습일이 오늘 또는 어제일 때만 연속일 카운트. 그 이전에 끊겼으면 0. */
    private long computeConsecutiveDays(Long userId) {
        List<LocalDate> dates = userStudyLogRepository.findDistinctStudyDatesDesc(userId)
            .stream()
            .map(LocalDateTime::toLocalDate)
            .distinct()
            .collect(Collectors.toList());
            
        if (dates.isEmpty()) return 0;

        LocalDate today = LocalDate.now();
        LocalDate latest = dates.get(0);
        if (!latest.equals(today) && !latest.equals(today.minusDays(1))) return 0;

        long count = 1;
        for (int i = 1; i < dates.size(); i++) {
            if (dates.get(i).equals(dates.get(i - 1).minusDays(1))) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }
}