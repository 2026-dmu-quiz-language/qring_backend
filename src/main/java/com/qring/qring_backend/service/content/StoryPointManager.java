package com.qring.qring_backend.service.content;

import com.qring.qring_backend.domain.user.UserAssetHistory.SourceType;
import com.qring.qring_backend.service.user.UserPointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 스토리 기능의 포인트 차감/환불을 짧은 독립 트랜잭션으로 처리한다.
 * OpenAI 호출을 트랜잭션 밖에 두기 위해 InteractiveStoryService 와 분리되어 있다
 * (같은 클래스 내부 호출에는 @Transactional 프록시가 적용되지 않으므로 별도 빈이어야 한다).
 * 실제 변경과 히스토리 기록은 UserPointService(포인트 원장)가 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StoryPointManager {

    private final UserPointService userPointService;

    /**
     * 포인트를 차감하고 남은 잔액을 반환한다. 잔액 부족이면 InsufficientPointsException(400).
     * 차감은 원자적 UPDATE 라 동시 요청이 겹쳐도 이중 차감되지 않는다. 히스토리에는 session_id 가 reference_key 로 남는다.
     */
    @Transactional
    public int deduct(Long userId, int amount, SourceType source, String sessionId) {
        return userPointService.spend(userId, amount, source, null, sessionId);
    }

    /** 차감했던 포인트를 되돌린다. 실패 시 예외가 나가므로 호출부가 잡아서 수동 복구용 로그를 남겨야 한다. */
    @Transactional
    public void refund(Long userId, int amount, String sessionId) {
        if (amount <= 0) {
            return;
        }
        userPointService.earn(userId, amount, SourceType.INTERACTIVE_STORY_REFUND, null, sessionId);
        log.info("[StoryPoint] 환불 완료 - userId: {}, amount: {}, sessionId: {}", userId, amount, sessionId);
    }

    @Transactional(readOnly = true)
    public int currentPoints(Long userId) {
        return userPointService.currentPoints(userId);
    }
}
