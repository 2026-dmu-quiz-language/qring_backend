package com.qring.qring_backend.service.content;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.domain.content.Content;
import com.qring.qring_backend.domain.content.ContentRepository;
import com.qring.qring_backend.domain.content.UserContentUnlock;
import com.qring.qring_backend.domain.content.UserContentUnlockRepository;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.domain.user.UserAsset;
import com.qring.qring_backend.domain.user.UserAssetHistory;
import com.qring.qring_backend.domain.user.UserAssetHistory.SourceType;
import com.qring.qring_backend.domain.user.UserAssetHistoryRepository;
import com.qring.qring_backend.domain.user.UserAssetRepository;
import com.qring.qring_backend.domain.user.UserLanguageLevelRepository;
import com.qring.qring_backend.dto.content.ContentUnlockResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentUnlockService {

    private final ContentRepository contentRepository;
    private final UserContentUnlockRepository userContentUnlockRepository;
    private final UserAssetRepository userAssetRepository;
    private final UserAssetHistoryRepository userAssetHistoryRepository;
    private final UserLanguageLevelRepository userLanguageLevelRepository;
    private final UserRepository userRepository;

    @Transactional
    public ContentUnlockResponseDto unlockContent(Long userId, Long contentId) {

        // 해금 언어는 클라이언트가 아니라 서버가 유저의 현재 학습 언어로 결정
        String language = getCurrentLanguage(userId);

        // 학습 중인 언어인지 먼저 검증 — 시작도 안 한 언어를 해금하는 걸 방지
        if (!userLanguageLevelRepository.existsByUserIdAndLanguage(userId, language)) {
            throw new IllegalArgumentException("학습 중인 언어가 아닙니다.");
        }

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("콘텐츠를 찾을 수 없습니다."));

        int requiredPoints = content.getRequiredPoints() == null ? 0 : content.getRequiredPoints();

        // 무료 콘텐츠거나 이미 해금했으면 그대로 UNLOCKED 응답 (중복 결제 방지)
        if (requiredPoints == 0) {
            return new ContentUnlockResponseDto(contentId, "UNLOCKED", 0, null);
        }
        if (userContentUnlockRepository.existsByUserIdAndContentContentIdAndLanguage(userId, contentId, language)) {
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
        unlock.setLanguage(language);
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

    /**
     * 스토리 내용에 접근하는 API(상세/스크립트/퀴즈/학습 시작) 맨 앞에서 호출.
     * 유료 콘텐츠인데 현재 언어로 해금하지 않았으면 ContentLockedException(403).
     */
    @Transactional(readOnly = true)
    public void validateAccessible(Long userId, Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("콘텐츠를 찾을 수 없습니다."));

        int requiredPoints = content.getRequiredPoints() == null ? 0 : content.getRequiredPoints();
        if (requiredPoints == 0) {
            return;
        }

        String language = getCurrentLanguage(userId);
        if (!userContentUnlockRepository.existsByUserIdAndContentContentIdAndLanguage(userId, contentId, language)) {
            throw new ContentLockedException();
        }
    }

    private String getCurrentLanguage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        return user.getLanguage();
    }
}