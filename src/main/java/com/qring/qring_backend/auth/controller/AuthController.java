package com.qring.qring_backend.auth.controller;

import com.qring.qring_backend.auth.dto.AuthRequest;
import com.qring.qring_backend.auth.dto.AuthResponse;
import com.qring.qring_backend.auth.dto.LoginResponse;
import com.qring.qring_backend.auth.dto.SignUpResponse;
import com.qring.qring_backend.auth.dto.UserDto;
import com.qring.qring_backend.auth.dto.VerifyEmailResponse;
import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.auth.service.AuthService;
import com.qring.qring_backend.domain.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 인증/계정 관련 HTTP 엔드포인트. 모든 경로는 {@code /api/v1/auth} 하위에 매핑된다. */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    /* ---------- 로컬 회원가입 & 이메일 인증 ---------- */

    /** 회원가입 1단계: 미인증 상태로 저장 + 인증 코드 발송. */
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody AuthRequest.SignUp request) {
        return ResponseEntity.ok(authService.signUp(request));
    }

    /** 회원가입 2단계: 이메일 인증 코드 검증 후 토큰 발급. 사용자 정보는 /dash·/me에서 조회. */
    @PostMapping("/verify-email")
    public ResponseEntity<VerifyEmailResponse> verifyEmail(@Valid @RequestBody AuthRequest.VerifyEmail request) {
        return ResponseEntity.ok(authService.verifyEmail(request));
    }

    /** 인증 코드 재발송. */
    @PostMapping("/resend-code")
    public ResponseEntity<Map<String, String>> resendCode(@Valid @RequestBody AuthRequest.ResendCode request) {
        authService.resendCode(request);
        return ResponseEntity.ok(Map.of("message", "인증 코드가 재발송되었습니다."));
    }

    /* ---------- 로그인 / 로그아웃 / 리프레시 ---------- */

    /** 이메일·비밀번호 로그인. 응답은 토큰 페어만, 사용자 정보는 /dash·/me에서 조회. */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody AuthRequest.Login request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /** 로그아웃 (서버는 무상태이므로 성공 응답만 반환). */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        return ResponseEntity.ok(Map.of(
            "logoutStatus", "SUCCESS",
            "message", "로그아웃 되었습니다."
        ));
    }

    /** 리프레시 토큰으로 액세스/리프레시 토큰 재발급. 응답은 토큰 페어만. */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody AuthRequest.RefreshToken request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    /* ---------- 소셜 로그인 ---------- */

    /** Google ID 토큰 기반 로그인. */
    @PostMapping("/oauth/google")
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody AuthRequest.SocialLogin request) {
        return ResponseEntity.ok(authService.googleLogin(request));
    }

    /** Kakao 인가 코드 기반 로그인. */
    @PostMapping("/oauth/kakao")
    public ResponseEntity<AuthResponse> kakaoLogin(@Valid @RequestBody AuthRequest.SocialLogin request) {
        return ResponseEntity.ok(authService.kakaoLogin(request));
    }

    /** LINE 인가 코드 기반 로그인. */
    @PostMapping("/oauth/line")
    public ResponseEntity<AuthResponse> lineLogin(@Valid @RequestBody AuthRequest.SocialLogin request) {
        return ResponseEntity.ok(authService.lineLogin(request));
    }

    /* ---------- 중복 체크 / 내 정보 ---------- */

    /** 이메일 사용 가능 여부 확인 (미인증 상태로만 존재하는 이메일은 재사용 가능). */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean available = userRepository.findByEmail(email)
            .map(u -> !Boolean.TRUE.equals(u.getEmailVerified()))
            .orElse(true);
        return ResponseEntity.ok(Map.of("available", available));
    }

    /** 닉네임 사용 가능 여부 확인. */
    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Boolean>> checkNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(Map.of("available", !userRepository.existsByNickname(nickname)));
    }

    /** 현재 로그인된 사용자 정보 조회. */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
        return ResponseEntity.ok(UserDto.from(user));
    }

    /* ---------- 학습 설정 (Onboarding) ---------- */

    /** 학습 설정(언어/레벨) 업데이트. 소셜 가입 직후 OnboardingScreen에서 호출. */
    @PutMapping("/preferences")
    public ResponseEntity<Map<String, Boolean>> updatePreferences(
        Authentication authentication,
        @Valid @RequestBody AuthRequest.UpdatePreferences request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        authService.updatePreferences(userId, request);
        return ResponseEntity.ok(Map.of("updated", true));
    }

}
