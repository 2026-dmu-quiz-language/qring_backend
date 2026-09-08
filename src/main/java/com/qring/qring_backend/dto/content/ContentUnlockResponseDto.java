package com.qring.qring_backend.dto.content;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ContentUnlockResponseDto {
    private Long contentId;
    private String status;         // 항상 "UNLOCKED" (실패 시 예외로 처리됨)
    private Integer spentPoints;   // 이번에 실제로 차감된 포인트 (이미 해금됐거나 무료면 0)
    private Integer balanceAfter;  // 차감 후 잔여 포인트 (무료 콘텐츠면 null)
}