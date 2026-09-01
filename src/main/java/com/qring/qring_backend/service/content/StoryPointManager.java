package com.qring.qring_backend.service.content;

import com.qring.qring_backend.domain.user.UserAsset;
import com.qring.qring_backend.domain.user.UserAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 스토리 기능의 포인트 차감/환불을 짧은 독립 트랜잭션으로 처리한다.
 * OpenAI 호출을 트랜잭션 밖에 두기 위해 InteractiveStoryService 와 분리되어 있다
 * (같은 클래스 내부 호출에는 @Transactional 프록시가 적용되지 않으므로 별도 빈이어야 한다).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StoryPointManager {

    private final UserAssetRepository userAssetRepository;

    /**
     * 포인트를 차감하고 남은 잔액을 반환한다. 잔액 부족이면 예외.
     * 차감은 원자적 UPDATE 라 동시 요청이 겹쳐도 이중 차감되지 않는다.
     */
    @Transactional
    public int deduct(Long userId, int amount) {
        if (amount <= 0) {
            return currentPoints(userId);
        }
        int updated = userAssetRepository.tryDeductPoints(userId, amount);
        if (updated == 0) {
            int current = currentPoints(userId);
            throw new IllegalStateException(String.format("포인트가 부족합니다. (필요: %d pt, 보유: %d pt)", amount, current));
        }
        return currentPoints(userId);
    }

    /** 차감했던 포인트를 되돌린다. 실패 시 예외가 나가므로 호출부가 잡아서 수동 복구용 로그를 남겨야 한다. */
    @Transactional
    public void refund(Long userId, int amount) {
        if (amount <= 0) {
            return;
        }
        userAssetRepository.addPoints(userId, amount);
        log.info("[StoryPoint] 환불 완료 - userId: {}, amount: {}", userId, amount);
    }

    @Transactional(readOnly = true)
    public int currentPoints(Long userId) {
        return userAssetRepository.findByUserUserId(userId)
                .map(UserAsset::getCurrentPoints)
                .map(p -> p != null ? p : 0)
                .orElseThrow(() -> new IllegalArgumentException("사용자 자산 정보(포인트)를 찾을 수 없습니다."));
    }
}
