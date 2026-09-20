package com.qring.qring_backend.auth.controller;

import com.qring.qring_backend.auth.service.UserWithdrawalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 사용자 계정 자체에 대한 엔드포인트 (탈퇴). 인증 필수 — SecurityConfig 의 anyRequest().authenticated() 에 걸린다. */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserWithdrawalService userWithdrawalService;

    /**
     * 회원 탈퇴 (하드 삭제). 액세스 토큰의 본인만 탈퇴할 수 있다.
     * 정식 경로는 /api/v1/users/withdraw. 프론트 화면마다 BASE_URL 이 "https://q-ring.app" 또는
     * "https://q-ring.app/api/v1" 로 달라 "${BASE_URL}/api/users/withdraw" 가 두 가지로 조합되므로
     * 그 두 경로도 함께 받는다. 프론트가 정식 경로로 정리되면 나머지 둘은 제거한다.
     */
    @DeleteMapping({"/api/v1/users/withdraw", "/api/users/withdraw", "/api/v1/api/users/withdraw"})
    public ResponseEntity<Map<String, Object>> withdraw(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        userWithdrawalService.withdraw(userId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "탈퇴 처리가 완료되었습니다."
        ));
    }
}
