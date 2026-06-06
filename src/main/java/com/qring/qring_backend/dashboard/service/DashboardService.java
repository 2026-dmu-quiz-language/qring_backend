package com.qring.qring_backend.dashboard.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.dashboard.dto.DashboardResponse;
import com.qring.qring_backend.domain.difficulty.DifficultyLevel;
import com.qring.qring_backend.domain.difficulty.DifficultyLevelRepository;
import com.qring.qring_backend.domain.quiz.AchievementCommentRepository;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.domain.user.UserStudyLogRepository;
import com.qring.qring_backend.domain.user.UserprogressRepository;

import lombok.RequiredArgsConstructor;

/** 대시보드용 통계 집계: 평균 진도율, 완료 스토리 수, 연속 학습일, 성취 코멘트, 난이도 설명. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final UserRepository userRepository;
    private final UserprogressRepository userprogressRepository;
    private final UserStudyLogRepository userStudyLogRepository;
    private final DifficultyLevelRepository difficultyLevelRepository;
    private final AchievementCommentRepository achievementCommentRepository;

    /** 사용자별 대시보드 응답 조립. 평균 진도율은 반올림 정수, 코멘트/레벨 설명은 옵션. */
    public DashboardResponse getDashboard(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));

        int progressRate = computeWeeklyProgressRate(userId);

        long completedStoryCount = userprogressRepository.countCompletedStories(userId);

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

        return DashboardResponse.builder()
            .name(user.getNickname())
            .consecutiveDays(consecutiveDays)
            .progressRate(progressRate)
            .commentText(commentText)
            .completedStoryCount(completedStoryCount)
            .levelDesc(levelDesc)
            .levelCode(levelCode)
            .weeklyStudy(weeklyStudy)
            .build();
    }

    /** 이번 주 성취도: 월~금 학습 시 +10%, 토~일 +25%. 매주 월요일 자동 초기화. */
    private int computeWeeklyProgressRate(Long userId) {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        Set<LocalDate> studied = new HashSet<>(
            userStudyLogRepository.findStudyDatesBetween(userId, monday, monday.plusDays(6))
        );
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
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        Set<LocalDate> studied = new HashSet<>(
            userStudyLogRepository.findStudyDatesBetween(userId, monday, monday.plusDays(6))
        );
        boolean[] result = new boolean[7];
        for (int i = 0; i < 7; i++) {
            result[i] = studied.contains(monday.plusDays(i));
        }
        return result;
    }

    /** 가장 최근 학습일이 오늘 또는 어제일 때만 연속일 카운트. 그 이전에 끊겼으면 0. */
    private long computeConsecutiveDays(Long userId) {
        List<LocalDate> dates = userStudyLogRepository.findDistinctStudyDatesDesc(userId);
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
