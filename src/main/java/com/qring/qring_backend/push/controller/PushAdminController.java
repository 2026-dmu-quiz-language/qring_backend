package com.qring.qring_backend.push.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.qring.qring_backend.auth.security.AdminAccessGuard;
import com.qring.qring_backend.push.dto.PushTestRequest;
import com.qring.qring_backend.push.dto.WrongAnswerReminderRunResult;
import com.qring.qring_backend.push.service.PushMessage;
import com.qring.qring_backend.push.service.PushSendResult;
import com.qring.qring_backend.push.service.PushSender;
import com.qring.qring_backend.push.service.PushTokenService;
import com.qring.qring_backend.push.service.WrongAnswerReminderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 푸시 관리자 API (ADMIN_USER_IDS 등록자만). 실기기 검증용 —
 *   - 특정 사용자 기기로 테스트 푸시
 *   - 오답 N일차 알림을 스케줄 시각을 기다리지 않고 즉시 실행 (대상 생성일 지정 가능)
 */
@Tag(name = "Push Admin")
@RestController
@RequestMapping("/admin/push")
@RequiredArgsConstructor
public class PushAdminController {

    private final AdminAccessGuard adminAccessGuard;
    private final PushTokenService pushTokenService;
    private final PushSender pushSender;
    private final WrongAnswerReminderService reminderService;

    @Operation(summary = "사용자 기기 전부에 테스트 푸시 (관리자)")
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> sendTest(Authentication authentication,
                                                        @Valid @RequestBody PushTestRequest request) {
        adminAccessGuard.checkAdmin((Long) authentication.getPrincipal());
        List<String> tokens = pushTokenService.tokensOf(request.getUserId());
        String title = blankToDefault(request.getTitle(), "Qring 테스트 알림");
        String body = blankToDefault(request.getBody(), "푸시 연결이 정상입니다.");
        PushSendResult r = pushSender.send(tokens, new PushMessage(title, body, Map.of("type", "TEST")));
        int removed = pushTokenService.removeInvalid(r.invalidTokens());
        return ResponseEntity.ok(Map.of(
                "tokens", tokens.size(),
                "success", r.successCount(),
                "failure", r.failureCount(),
                "removedInvalidTokens", removed));
    }

    @Operation(summary = "오답 N일차 알림 즉시 실행 (관리자). createdDate 생략 시 오늘-days-after")
    @PostMapping("/wrong-answer-reminder/run")
    public ResponseEntity<WrongAnswerReminderRunResult> runReminder(
            Authentication authentication,
            @RequestParam(value = "createdDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdDate) {
        adminAccessGuard.checkAdmin((Long) authentication.getPrincipal());
        WrongAnswerReminderRunResult result = createdDate == null
                ? reminderService.runForToday()
                : reminderService.run(createdDate);
        return ResponseEntity.ok(result);
    }

    private static String blankToDefault(String value, String def) {
        return value == null || value.isBlank() ? def : value;
    }
}
