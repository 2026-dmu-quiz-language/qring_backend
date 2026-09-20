package com.qring.qring_backend.push.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qring.qring_backend.push.dto.PushTokenRequest;
import com.qring.qring_backend.push.service.PushTokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 기기 푸시 토큰 등록·해제. 경로: /api/v1/push (JWT 필요).
 * 프로젝트 관례(다른 도메인 API 가 전부 POST)에 맞춰 해제도 POST 로 둔다 — RN fetch 의 DELETE 본문 처리 차이도 피한다.
 */
@Tag(name = "Push")
@RestController
@RequestMapping("/api/v1/push")
@RequiredArgsConstructor
public class PushTokenController {

    private final PushTokenService pushTokenService;

    @Operation(summary = "FCM 기기 토큰 등록 (로그인 직후·토큰 갱신 시 호출, 같은 토큰은 소유자·시각만 갱신)")
    @PostMapping("/token")
    public ResponseEntity<Map<String, Object>> register(Authentication authentication,
                                                        @Valid @RequestBody PushTokenRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        pushTokenService.register(userId, request.getToken(), request.getPlatform());
        return ResponseEntity.ok(Map.of("registered", true));
    }

    @Operation(summary = "FCM 기기 토큰 해제 (로그아웃 시 호출). 본인 토큰이 아니면 removed=false")
    @PostMapping("/token/delete")
    public ResponseEntity<Map<String, Object>> unregister(Authentication authentication,
                                                          @Valid @RequestBody PushTokenRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        boolean removed = pushTokenService.unregister(userId, request.getToken());
        return ResponseEntity.ok(Map.of("removed", removed));
    }
}
