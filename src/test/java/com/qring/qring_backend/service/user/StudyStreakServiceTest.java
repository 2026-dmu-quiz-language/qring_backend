package com.qring.qring_backend.service.user;

import com.qring.qring_backend.domain.user.UserAssetHistory.SourceType;
import com.qring.qring_backend.domain.user.UserAssetRepository;
import com.qring.qring_backend.domain.user.UserStudyLogRepository;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 연속 학습일 계산과 주기 보상.
 * 회귀 방지 두 가지 — 지급 시점이 대시보드 조회가 아니라 학습 시점이라는 것,
 * 그리고 연속이 끊겼다 다시 채우면 가드를 되돌려 재달성 보상이 나간다는 것 (#173).
 */
class StudyStreakServiceTest {

    private static final long USER_ID = 19L;

    private UserStudyLogRepository userStudyLogRepository;
    private UserAssetRepository userAssetRepository;
    private UserPointService userPointService;
    private StudyStreakService service;

    @BeforeEach
    void setUp() {
        userStudyLogRepository = mock(UserStudyLogRepository.class);
        userAssetRepository = mock(UserAssetRepository.class);
        userPointService = mock(UserPointService.class);
        service = new StudyStreakService(userStudyLogRepository, userAssetRepository, userPointService);
    }

    /* ---------------- 연속일 계산 ---------------- */

    @Test
    @DisplayName("오늘 포함 연속이면 그 길이, 어제가 마지막이어도 이어서 센다")
    void currentStreak_countsUntilGap() {
        stubStudyDates(0, 1, 2);
        assertEquals(3, service.currentStreak(USER_ID));

        stubStudyDates(1, 2, 3, 4, 5);   // 어제까지 5일, 오늘은 아직 미학습
        assertEquals(5, service.currentStreak(USER_ID));
    }

    @Test
    @DisplayName("그저께가 마지막이거나 기록이 없으면 0")
    void currentStreak_brokenOrEmpty() {
        stubStudyDates(2, 3, 4);
        assertEquals(0, service.currentStreak(USER_ID));

        when(userStudyLogRepository.findDistinctStudyDatesDesc(USER_ID)).thenReturn(List.of());
        assertEquals(0, service.currentStreak(USER_ID));
    }

    @Test
    @DisplayName("중간에 하루가 비면 거기서 끊어 센다")
    void currentStreak_stopsAtGap() {
        stubStudyDates(0, 1, 3, 4);   // 2일 전이 비어 있음
        assertEquals(2, service.currentStreak(USER_ID));
    }

    /* ---------------- 보상 지급 ---------------- */

    @Test
    @DisplayName("15일째 학습하면 그 자리에서 +30 지급 (대시보드를 열지 않아도 받는다)")
    void awardIfDue_fifteenthDay() {
        guard(0);
        stubConsecutive(15);
        when(userAssetRepository.updateStreakDaysIfHigher(USER_ID, 15)).thenReturn(1);

        assertTrue(service.awardIfDue(USER_ID));

        verify(userAssetRepository).updateStreakDaysIfHigher(USER_ID, 15);
        verify(userPointService).earn(USER_ID, StudyStreakService.REWARD_POINTS, SourceType.STREAK_REWARD, 15L);
    }

    @Test
    @DisplayName("주기에 못 미치면 지급하지 않는다")
    void awardIfDue_notYet() {
        guard(0);
        stubConsecutive(14);

        assertFalse(service.awardIfDue(USER_ID));

        verify(userAssetRepository, never()).updateStreakDaysIfHigher(anyLong(), anyInt());
        verify(userPointService, never()).earn(anyLong(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("같은 날 두 번 학습해도 한 번만 — 가드 선점에 실패하면 지급하지 않는다")
    void awardIfDue_secondStudySameDay() {
        guard(15);
        stubConsecutive(15);

        assertFalse(service.awardIfDue(USER_ID));

        verify(userPointService, never()).earn(anyLong(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("연속이 끊겼다 다시 15일을 채우면 가드를 0 으로 되돌리고 지급한다 (#173)")
    void awardIfDue_afterBrokenStreak() {
        guard(30);          // 예전에 30일까지 받았고
        stubConsecutive(15); // 끊긴 뒤 새로 15일을 채운 상태
        when(userAssetRepository.updateStreakDaysIfHigher(USER_ID, 15)).thenReturn(1);

        assertTrue(service.awardIfDue(USER_ID));

        verify(userAssetRepository).updateStreakDays(USER_ID, 0);   // 리셋이 먼저
        verify(userAssetRepository).updateStreakDaysIfHigher(USER_ID, 15);
        verify(userPointService).earn(USER_ID, StudyStreakService.REWARD_POINTS, SourceType.STREAK_REWARD, 15L);
    }

    @Test
    @DisplayName("끊긴 뒤 아직 주기에 못 미치면 가드만 되돌리고 지급하지 않는다")
    void awardIfDue_afterBrokenStreak_notYet() {
        guard(30);
        stubConsecutive(3);

        assertFalse(service.awardIfDue(USER_ID));

        verify(userAssetRepository).updateStreakDays(USER_ID, 0);
        verify(userPointService, never()).earn(anyLong(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("다음 주기(30일)도 이어서 지급한다")
    void awardIfDue_nextInterval() {
        guard(15);
        stubConsecutive(30);
        when(userAssetRepository.updateStreakDaysIfHigher(USER_ID, 30)).thenReturn(1);

        assertTrue(service.awardIfDue(USER_ID));

        verify(userAssetRepository, never()).updateStreakDays(anyLong(), anyInt());   // 끊긴 적 없으니 리셋 없음
        verify(userPointService).earn(USER_ID, StudyStreakService.REWARD_POINTS, SourceType.STREAK_REWARD, 30L);
    }

    @Test
    @DisplayName("자산 row 가 없으면 학습 저장을 막지 않고 조용히 건너뛴다")
    void awardIfDue_noAssetRow() {
        when(userAssetRepository.findStreakDaysByUserId(USER_ID)).thenReturn(Optional.empty());

        assertFalse(service.awardIfDue(USER_ID));

        verifyNoInteractions(userPointService, userStudyLogRepository);
    }

    /* ---------------- fixtures ---------------- */

    private void guard(int lastRewardedStreak) {
        when(userAssetRepository.findStreakDaysByUserId(USER_ID)).thenReturn(Optional.of(lastRewardedStreak));
    }

    /** 오늘부터 과거로 n 일 연속 학습. */
    private void stubConsecutive(int n) {
        stubStudyDates(IntStream.range(0, n).toArray());
    }

    /** daysAgo 로 준 날짜들(오늘=0)을 학습일로 돌려주게 한다. 내림차순으로 정렬해 넘긴다. */
    private void stubStudyDates(int... daysAgo) {
        LocalDateTime noonToday = LocalDate.now().atTime(12, 0);
        List<LocalDateTime> dates = IntStream.of(daysAgo).sorted()
                .mapToObj(noonToday::minusDays)
                .toList();
        when(userStudyLogRepository.findDistinctStudyDatesDesc(USER_ID)).thenReturn(dates);
    }
}
