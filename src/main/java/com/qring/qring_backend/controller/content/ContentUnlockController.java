package com.qring.qring_backend.controller.content;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qring.qring_backend.dto.content.ContentUnlockResponseDto;
import com.qring.qring_backend.service.content.ContentUnlockService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ContentUnlockController {

    private final ContentUnlockService contentUnlockService;

    @Operation(summary = "콘텐츠 해금 - 포인트 차감 후 유저별 영구 해금 처리")
    @PostMapping("/content/{contentId}/unlock")
    public ResponseEntity<ContentUnlockResponseDto> unlockContent(
            Authentication authentication,
            @PathVariable Long contentId) {

        Long userId = (Long) authentication.getPrincipal();
        ContentUnlockResponseDto response = contentUnlockService.unlockContent(userId, contentId);
        return ResponseEntity.ok(response);
    }
}