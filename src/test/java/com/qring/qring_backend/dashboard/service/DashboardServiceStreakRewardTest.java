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
import com.qring.qring_backend.domain.user.UserAssetRepository;
import com.qring.qring_backend.domain.user.UserStudyLogRepository;
import com.qring.qring_backend.domain.user.UserprogressRepository;
import com.qring.qring_backend.service.user.UserPointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 15일 연속 보상: 원장(UserPointService)으로 지급하고 가드값은 벌크 UPDATE 로 갱신한다.
 * 회귀 방지 — 예전엔 UserAsset 엔티티를 save 해서 벌크로 올린 포인트를 옛 값으로 덮어썼다 (80 → 50).
 */
class DashboardServiceStreakRewardTest {

    private static final long USER_ID = 19L;

    private UserStudyLogRepository userStudyLogRepository;
    private UserAssetRepository userAssetRepository;
    private UserPointService userPointService;
    private DashboardService service;

    @BeforeEach
    void setUp() {
        UserRepository userRepository = mock(UserRepository.class);
        userStudyLogRepository = mock(UserStudyLogRepository.class);
        userAssetRepository = mock(UserAssetRepository.class);
        userPointService = mock(UserPointService.class);

        service = new DashboardService(userRepository, mock(UserprogressRepository.class), userStudyLogRepository,
                mock(DifficultyLevelRepository.class), mock(AchievementCommentRepository.class),
                mock(WrongAnswerRepository.class), mock(CompetitionWrongAnswerRepository.class),
                userAssetRepository, userPointService,
                mock(StoryProgressRepository.class), mock(QuizResultRepository.class));

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(
                User.builder().userId(USER_ID).nickname("Yarr").language("EN").build()));
        when(userAssetRepository.findCurrentPointsByUserId(USER_ID)).thenReturn(Optional.of(50));
    }

    @Test
    @DisplayName("15일 연속 + 미지급이면 원장으로 +30 지급, streak_days 는 벌크 UPDATE, 엔티티 save 없음")
    void fifteenDays_rewardsThroughLedger() {
        stubConsecutiveDays(15);
        when(userAssetRepository.findStreakDaysByUserId(USER_ID)).thenReturn(Optional.of(0));
        when(userPointService.earn(USER_ID, 30, SourceType.STREAK_REWARD, 15L)).thenReturn(80);

        DashboardResponse res = service.getDashboard(USER_ID);

        assertEquals(15, res.getConsecutiveDays());
        assertTrue(res.isConsecutivePointReceived());
        assertEquals(80, res.getCurrentPoints());
        verify(userAssetRepository).updateStreakDays(USER_ID, 15);
        verify(userAssetRepository, never()).save(any());
        verify(userAssetRepository, never()).findByUserUserId(anyLong());
    }

    @Test
    @DisplayName("같은 15일에 다시 조회하면(streak_days=15) 중복 지급하지 않는다")
    void sameStreak_alreadyRewarded_noDuplicate() {
        stubConsecutiveDays(15);
        when(userAssetRepository.findStreakDaysByUserId(USER_ID)).thenReturn(Optional.of(15));

        DashboardResponse res = service.getDashboard(USER_ID);

        assertFalse(res.isConsecutivePointReceived());
        assertEquals(50, res.getCurrentPoints());
        verify(userPointService, never()).earn(anyLong(), anyInt(), any(), any());
        verify(userAssetRepository, never()).updateStreakDays(anyLong(), anyInt());
    }

    @Test
    @DisplayName("15의 배수가 아니면 지급하지 않는다")
    void notMultipleOfFifteen_noReward() {
        stubConsecutiveDays(14);

        DashboardResponse res = service.getDashboard(USER_ID);

        assertEquals(14, res.getConsecutiveDays());
        assertFalse(res.isConsecutivePointReceived());
        assertEquals(50, res.getCurrentPoints());
        verify(userPointService, never()).earn(anyLong(), anyInt(), any(), any());
    }

    /** 오늘부터 과거로 n 일 연속 학습 로그. */
    private void stubConsecutiveDays(int n) {
        LocalDateTime today = LocalDate.now().atTime(12, 0);
        List<LocalDateTime> dates = IntStream.range(0, n).mapToObj(today::minusDays).toList();
        when(userStudyLogRepository.findDistinctStudyDatesDesc(USER_ID)).thenReturn(dates);
    }
}
