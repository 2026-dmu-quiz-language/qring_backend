package com.qring.qring_backend.service.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.domain.user.UserAsset;
import com.qring.qring_backend.domain.user.UserAssetHistory;
import com.qring.qring_backend.domain.user.UserAssetHistory.SourceType;
import com.qring.qring_backend.domain.user.UserAssetHistoryRepository;
import com.qring.qring_backend.domain.user.UserAssetRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 포인트 원장. 포인트가 바뀌는 모든 경로는 이 서비스를 거친다 — 잔액 변경과 user_asset_history 기록을
 * 같은 트랜잭션에서 함께 처리하므로 히스토리 누락이 구조적으로 생기지 않는다.
 *
 * 잔액 변경은 원자적 벌크 UPDATE(동시 요청 이중 차감 방지)이고, 변경 후 잔액은 스칼라 쿼리로 DB 에서
 * 다시 읽는다. 호출부가 UserAsset 엔티티를 미리 조회해 두었다면 그 값은 갱신 전 값이며, 그 엔티티를
 * save 하면 벌크 UPDATE 결과를 옛 값으로 덮어쓴다 (대시보드 15일 보상이 지급 직후 사라지던 원인).
 * 그래서 호출부는 엔티티 대신 이 서비스가 돌려주는 잔액을 써야 한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserPointService {

    /** 가입 완료 시 지급하는 초기 포인트. */
    public static final int SIGNUP_POINTS = 50;

    private final UserAssetRepository userAssetRepository;
    private final UserAssetHistoryRepository userAssetHistoryRepository;

    /** 적립. 변경 후 잔액을 돌려준다. amount 가 0 이면 기록 없이 잔액만 돌려준다. */
    @Transactional
    public int earn(Long userId, int amount, SourceType source, Long referenceId) {
        return earn(userId, amount, source, referenceId, null);
    }

    /** 적립 (참조가 숫자 id 가 아닌 경우 — 인터랙티브 스토리 세션 id 등 — referenceKey 로 남긴다). */
    @Transactional
    public int earn(Long userId, int amount, SourceType source, Long referenceId, String referenceKey) {
        requireNonNegative(amount);
        if (amount == 0) {
            return currentPoints(userId);
        }
        userAssetRepository.addPoints(userId, amount);
        int balance = currentPoints(userId);
        record(userId, amount, balance, source, referenceId, referenceKey);
        log.info("[Point] +{} pt userId={} source={} ref={}/{} balance={}", amount, userId, source, referenceId, referenceKey, balance);
        return balance;
    }

    /** 차감. 잔액 부족이면 InsufficientPointsException(400). 변경 후 잔액을 돌려준다. */
    @Transactional
    public int spend(Long userId, int amount, SourceType source, Long referenceId) {
        return spend(userId, amount, source, referenceId, null);
    }

    @Transactional
    public int spend(Long userId, int amount, SourceType source, Long referenceId, String referenceKey) {
        requireNonNegative(amount);
        if (amount == 0) {
            return currentPoints(userId);
        }
        int updated = userAssetRepository.tryDeductPoints(userId, amount);
        if (updated == 0) {
            throw new InsufficientPointsException(amount, currentPoints(userId));
        }
        int balance = currentPoints(userId);
        record(userId, -amount, balance, source, referenceId, referenceKey);
        log.info("[Point] -{} pt userId={} source={} ref={}/{} balance={}", amount, userId, source, referenceId, referenceKey, balance);
        return balance;
    }

    /** 가입 완료 시 자산 row 생성 + 초기 포인트를 히스토리 첫 줄(SIGNUP_BONUS)로 기록. 이미 있으면 아무것도 하지 않는다. */
    @Transactional
    public void initAssetIfAbsent(User user) {
        if (userAssetRepository.findByUserUserId(user.getUserId()).isPresent()) {
            return;
        }
        UserAsset asset = new UserAsset();
        asset.setUser(user);
        asset.setCurrentPoints(SIGNUP_POINTS);
        asset.setTotalExp(0);
        asset.setStreakDays(0);
        userAssetRepository.save(asset);
        record(user.getUserId(), SIGNUP_POINTS, SIGNUP_POINTS, SourceType.SIGNUP_BONUS, null, null);
    }

    /** 현재 잔액 (DB 스칼라 조회, 영속성 컨텍스트의 옛 엔티티 값에 영향받지 않음). 자산 row 가 없으면 예외. */
    @Transactional(readOnly = true)
    public int currentPoints(Long userId) {
        return userAssetRepository.findCurrentPointsByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 자산 정보를 찾을 수 없습니다."));
    }

    private void record(Long userId, int changeAmount, int balanceAfter,
                        SourceType source, Long referenceId, String referenceKey) {
        UserAssetHistory history = new UserAssetHistory();
        history.setUserId(userId);
        history.setChangeAmount(changeAmount);
        history.setBalanceAfter(balanceAfter);
        history.setSourceType(source);
        history.setReferenceId(referenceId);
        history.setReferenceKey(referenceKey);
        userAssetHistoryRepository.save(history);
    }

    private static void requireNonNegative(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("포인트 변동 금액은 0 이상이어야 합니다: " + amount);
        }
    }
}
