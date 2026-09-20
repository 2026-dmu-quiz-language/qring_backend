package com.qring.qring_backend.service.content;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qring.qring_backend.domain.content.Content;
import com.qring.qring_backend.domain.content.ContentRepository;
import com.qring.qring_backend.domain.content.UserContentUnlock;
import com.qring.qring_backend.domain.content.UserContentUnlockRepository;
import com.qring.qring_backend.domain.user.UserAssetHistory.SourceType;
import com.qring.qring_backend.dto.content.ContentUnlockResponseDto;
import com.qring.qring_backend.service.user.UserPointService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentUnlockService {

    private final ContentRepository contentRepository;
    private final UserContentUnlockRepository userContentUnlockRepository;
    private final UserPointService userPointService;

    @Transactional
    public ContentUnlockResponseDto unlockContent(Long userId, Long contentId) {

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("콘텐츠를 찾을 수 없습니다."));

        int requiredPoints = content.getRequiredPoints() == null ? 0 : content.getRequiredPoints();

        // 무료 콘텐츠거나 이미 해금했으면 그대로 UNLOCKED 응답 (중복 결제 방지)
        if (requiredPoints == 0) {
            return new ContentUnlockResponseDto(contentId, "UNLOCKED", 0, null);
        }
        if (userContentUnlockRepository.existsByUserIdAndContentContentId(userId, contentId)) {
            return new ContentUnlockResponseDto(contentId, "UNLOCKED", 0, userPointService.currentPoints(userId));
        }

        // 원자적 차감 + 히스토리 (잔액 부족이면 InsufficientPointsException → 400)
        int balanceAfter = userPointService.spend(userId, requiredPoints, SourceType.CONTENT_UNLOCK, contentId);

        UserContentUnlock unlock = new UserContentUnlock();
        unlock.setUserId(userId);
        unlock.setContent(content);
        userContentUnlockRepository.save(unlock);

        return new ContentUnlockResponseDto(contentId, "UNLOCKED", requiredPoints, balanceAfter);
    }
}
