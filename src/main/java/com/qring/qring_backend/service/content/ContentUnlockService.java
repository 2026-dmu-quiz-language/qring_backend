package com.qring.qring_backend.service.content;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qring.qring_backend.domain.content.Content;
import com.qring.qring_backend.domain.content.ContentRepository;
import com.qring.qring_backend.domain.content.UserContentUnlock;
import com.qring.qring_backend.domain.content.UserContentUnlockRepository;
import com.qring.qring_backend.domain.user.UserAsset;
import com.qring.qring_backend.domain.user.UserAssetHistory;
import com.qring.qring_backend.domain.user.UserAssetHistory.SourceType;
import com.qring.qring_backend.domain.user.UserAssetHistoryRepository;
import com.qring.qring_backend.domain.user.UserAssetRepository;
import com.qring.qring_backend.dto.content.ContentUnlockResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentUnlockService {

    private final ContentRepository contentRepository;
    private final UserContentUnlockRepository userContentUnlockRepository;
    private final UserAssetRepository userAssetRepository;
    private final UserAssetHistoryRepository userAssetHistoryRepository;

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
            UserAsset asset = userAssetRepository.findByUserUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("유저 자산 정보를 찾을 수 없습니다."));
            return new ContentUnlockResponseDto(contentId, "UNLOCKED", 0, asset.getCurrentPoints());
        }

        UserAsset asset = userAssetRepository.findByUserUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 자산 정보를 찾을 수 없습니다."));

        // 원자적 차감 — 잔액 검사와 차감이 분리되어 동시 요청 시 음수 잔액이 가능하던 문제 방지
        int deducted = userAssetRepository.tryDeductPoints(userId, requiredPoints);
        if (deducted == 0) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
        int balanceAfter = asset.getCurrentPoints() - requiredPoints;

        UserContentUnlock unlock = new UserContentUnlock();
        unlock.setUserId(userId);
        unlock.setContent(content);
        userContentUnlockRepository.save(unlock);

        UserAssetHistory history = new UserAssetHistory();
        history.setUserId(userId);
        history.setChangeAmount(-requiredPoints);
        history.setBalanceAfter(balanceAfter);
        history.setSourceType(SourceType.CONTENT_UNLOCK);
        history.setReferenceId(contentId);
        userAssetHistoryRepository.save(history);

        return new ContentUnlockResponseDto(contentId, "UNLOCKED", requiredPoints, balanceAfter);
    }
}