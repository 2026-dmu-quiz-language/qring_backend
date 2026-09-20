package com.qring.qring_backend.service.user;

import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.domain.user.UserAsset;
import com.qring.qring_backend.domain.user.UserAssetHistory;
import com.qring.qring_backend.domain.user.UserAssetHistory.SourceType;
import com.qring.qring_backend.domain.user.UserAssetHistoryRepository;
import com.qring.qring_backend.domain.user.UserAssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 포인트 원장: 적립·차감마다 히스토리 한 건, 잔액은 DB 재조회, 잔액 부족은 기록 없이 400 예외, 가입 초기 지급 기록. */
class UserPointServiceTest {

    private static final long USER_ID = 19L;

    private UserAssetRepository userAssetRepository;
    private UserAssetHistoryRepository historyRepository;
    private UserPointService service;

    @BeforeEach
    void setUp() {
        userAssetRepository = mock(UserAssetRepository.class);
        historyRepository = mock(UserAssetHistoryRepository.class);
        service = new UserPointService(userAssetRepository, historyRepository);
    }

    @Test
    @DisplayName("적립: 벌크 UPDATE 후 DB 잔액을 돌려주고 +금액 히스토리를 남긴다")
    void earn_recordsHistoryWithDbBalance() {
        when(userAssetRepository.findCurrentPointsByUserId(USER_ID)).thenReturn(Optional.of(80));

        int balance = service.earn(USER_ID, 30, SourceType.STREAK_REWARD, 15L);

        assertEquals(80, balance);
        verify(userAssetRepository).addPoints(USER_ID, 30);
        verify(userAssetRepository, never()).save(any());   // 엔티티 save 로 덮어쓰지 않는다

        UserAssetHistory h = savedHistory();
        assertEquals(USER_ID, h.getUserId());
        assertEquals(30, h.getChangeAmount());
        assertEquals(80, h.getBalanceAfter());
        assertEquals(SourceType.STREAK_REWARD, h.getSourceType());
        assertEquals(15L, h.getReferenceId());
        assertNull(h.getReferenceKey());
    }

    @Test
    @DisplayName("차감: 성공 시 -금액 히스토리, reference_key(세션 id)도 남는다")
    void spend_recordsNegativeHistory() {
        when(userAssetRepository.tryDeductPoints(USER_ID, 400)).thenReturn(1);
        when(userAssetRepository.findCurrentPointsByUserId(USER_ID)).thenReturn(Optional.of(100));

        int balance = service.spend(USER_ID, 400, SourceType.INTERACTIVE_STORY_CREATE, null, "sess-abc");

        assertEquals(100, balance);
        UserAssetHistory h = savedHistory();
        assertEquals(-400, h.getChangeAmount());
        assertEquals(100, h.getBalanceAfter());
        assertEquals(SourceType.INTERACTIVE_STORY_CREATE, h.getSourceType());
        assertNull(h.getReferenceId());
        assertEquals("sess-abc", h.getReferenceKey());
    }

    @Test
    @DisplayName("차감: 잔액 부족이면 InsufficientPointsException 이고 히스토리를 남기지 않는다")
    void spend_insufficient_noHistory() {
        when(userAssetRepository.tryDeductPoints(USER_ID, 70)).thenReturn(0);
        when(userAssetRepository.findCurrentPointsByUserId(USER_ID)).thenReturn(Optional.of(50));

        InsufficientPointsException e = assertThrows(InsufficientPointsException.class,
                () -> service.spend(USER_ID, 70, SourceType.COMPETITION_ENTRY, 1L));

        assertEquals("포인트가 부족합니다.", e.getMessage());
        assertEquals(70, e.getRequired());
        assertEquals(50, e.getCurrent());
        verify(historyRepository, never()).save(any());
    }

    @Test
    @DisplayName("금액 0 이면 변경도 기록도 없이 잔액만 돌려준다 (보상 0, 보관 비용 0 케이스)")
    void zeroAmount_noChangeNoHistory() {
        when(userAssetRepository.findCurrentPointsByUserId(USER_ID)).thenReturn(Optional.of(50));

        assertEquals(50, service.earn(USER_ID, 0, SourceType.COMPETITION_REWARD, 1L));
        assertEquals(50, service.spend(USER_ID, 0, SourceType.INTERACTIVE_STORY_ARCHIVE, null, "sess-x"));

        verify(userAssetRepository, never()).addPoints(anyLong(), anyInt());
        verify(userAssetRepository, never()).tryDeductPoints(anyLong(), anyInt());
        verify(historyRepository, never()).save(any());
    }

    @Test
    @DisplayName("가입 초기 지급: 자산 row 생성 + SIGNUP_BONUS 히스토리(+50, 잔액 50). 이미 있으면 아무것도 안 한다")
    void initAssetIfAbsent() {
        User user = User.builder().userId(USER_ID).email("yarr@example.com").build();
        when(userAssetRepository.findByUserUserId(USER_ID)).thenReturn(Optional.empty());

        service.initAssetIfAbsent(user);

        ArgumentCaptor<UserAsset> asset = ArgumentCaptor.forClass(UserAsset.class);
        verify(userAssetRepository).save(asset.capture());
        assertEquals(UserPointService.SIGNUP_POINTS, asset.getValue().getCurrentPoints());
        assertEquals(0, asset.getValue().getStreakDays());

        UserAssetHistory h = savedHistory();
        assertEquals(SourceType.SIGNUP_BONUS, h.getSourceType());
        assertEquals(UserPointService.SIGNUP_POINTS, h.getChangeAmount());
        assertEquals(UserPointService.SIGNUP_POINTS, h.getBalanceAfter());

        // 두 번째 호출: 이미 있음
        when(userAssetRepository.findByUserUserId(USER_ID)).thenReturn(Optional.of(asset.getValue()));
        service.initAssetIfAbsent(user);
        verify(userAssetRepository).save(any());          // 여전히 1회
        verify(historyRepository).save(any());            // 여전히 1회
    }

    private UserAssetHistory savedHistory() {
        ArgumentCaptor<UserAssetHistory> captor = ArgumentCaptor.forClass(UserAssetHistory.class);
        verify(historyRepository).save(captor.capture());
        return captor.getValue();
    }
}
