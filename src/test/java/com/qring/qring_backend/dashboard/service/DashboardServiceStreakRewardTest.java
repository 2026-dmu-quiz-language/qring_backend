package com.qring.qring_backend.dashboard.service;

import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.dashboard.dto.DashboardResponse;
import com.qring.qring_backend.domain.competition.CompetitionWrongAnswerRepository;
import com.qring.qring_backend.domain.difficulty.DifficultyLevelRepository;
import com.qring.qring_backend.domain.quiz.AchievementCommentRepository;
import com.qring.qring_backend.domain.quiz.QuizResultRepository;
import com.qring.qring_backend.domain.quiz.StoryProgressRepository;
import com.qring.qring_backend.domain.quiz.WrongAnswerRepository;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.domain.user.UserAssetHistory.SourceType;
import com.qring.qring_backend.domain.user.UserAssetHistoryRepository;
import com.qring.qring_backend.domain.user.UserAssetRepository;
import com.qring.qring_backend.domain.user.UserStudyLogRepository;
import com.qring.qring_backend.domain.user.UserprogressRepository;
import com.qring.qring_backend.service.user.StudyStreakService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 대시보드는 연속 학습 보상을 지급하지 않는다 — 지급은 학습 시점(StudyStreakService)에 끝나고,
 * 여기서는 오늘 받았는지만 이력으로 읽어 화면에 알린다.
 * 회귀 방지 — 예전엔 조회 시점에 지급하면서 UserAsset 엔티티를 save 해 포인트를 덮어썼다 (80 → 50).
 */
class DashboardServiceStreakRewardTest {

    private static final long USER_ID = 19L;

    private UserAssetRepository userAssetRepository;
    private UserAssetHistoryRepository userAssetHistoryRepository;
    private StudyStreakService studyStreakService;
    private DashboardService service;

    @BeforeEach
    void setUp() {
        UserRepository userRepository = mock(UserRepository.class);
        userAssetRepository = mock(UserAssetRepository.class);
        userAssetHistoryRepository = mock(UserAssetHistoryRepository.class);
        studyStreakService = mock(StudyStreakService.class);

        service = new DashboardService(userRepository, mock(UserprogressRepository.class),
                mock(UserStudyLogRepository.class), mock(DifficultyLevelRepository.class),
                mock(AchievementCommentRepository.class),
                mock(WrongAnswerRepository.class), mock(CompetitionWrongAnswerRepository.class),
                userAssetRepository, userAssetHistoryRepository,
                mock(StoryProgressRepository.class), mock(QuizResultRepository.class), studyStreakService);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(
                User.builder().userId(USER_ID).nickname("Yarr").language("EN").build()));
        when(userAssetRepository.findCurrentPointsByUserId(USER_ID)).thenReturn(Optional.of(80));
    }

    @Test
    @DisplayName("15일째여도 대시보드는 지급하지 않는다 — 포인트도 가드도 건드리지 않는다")
    void dashboard_neverAwards() {
        when(studyStreakService.currentStreak(USER_ID)).thenReturn(15L);

        DashboardResponse res = service.getDashboard(USER_ID);

        assertEquals(15, res.getConsecutiveDays());
        assertEquals(80, res.getCurrentPoints());
        verify(studyStreakService, never()).awardIfDue(anyLong());
        verify(userAssetRepository, never()).updateStreakDays(anyLong(), anyInt());
        verify(userAssetRepository, never()).updateStreakDaysIfHigher(anyLong(), anyInt());
        verify(userAssetRepository, never()).save(any());
    }

    @Test
    @DisplayName("오늘 보상 이력이 있으면 isConsecutivePointReceived=true 로 알린다")
    void reportsTodaysReward() {
        when(studyStreakService.currentStreak(USER_ID)).thenReturn(15L);
        when(userAssetHistoryRepository.existsByUserIdAndSourceTypeBetween(
                eq(USER_ID), eq(SourceType.STREAK_REWARD), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);

        assertTrue(service.getDashboard(USER_ID).isConsecutivePointReceived());
    }

    @Test
    @DisplayName("오늘 보상 이력이 없으면 false")
    void noRewardToday() {
        when(studyStreakService.currentStreak(USER_ID)).thenReturn(7L);
        when(userAssetHistoryRepository.existsByUserIdAndSourceTypeBetween(
                eq(USER_ID), eq(SourceType.STREAK_REWARD), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);

        DashboardResponse res = service.getDashboard(USER_ID);

        assertEquals(7, res.getConsecutiveDays());
        assertFalse(res.isConsecutivePointReceived());
    }
}
