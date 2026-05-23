package com.qring.qring_backend.auth.controller;

import com.qring.qring_backend.auth.dto.AuthRequest;
import com.qring.qring_backend.auth.dto.AuthResponse;
import com.qring.qring_backend.auth.dto.SignUpResponse;
import com.qring.qring_backend.auth.dto.UserDto;
import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.auth.service.AuthService;
import com.qring.qring_backend.domain.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    /** 회원가입 2단계: 이메일 인증 코드 검증 후 토큰 발급. */
    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(@Valid @RequestBody AuthRequest.VerifyEmail request) {
        return ResponseEntity.ok(authService.verifyEmail(request));
    }

    /** 인증 코드 재발송. */
    @PostMapping("/resend-code")
    public ResponseEntity<Map<String, String>> resendCode(@Valid @RequestBody AuthRequest.ResendCode request) {
        authService.resendCode(request);
        return ResponseEntity.ok(Map.of("message", "인증 코드가 재발송되었습니다."));
    }

    /* ---------- 로그인 / 로그아웃 / 리프레시 ---------- */

    /** 이메일·비밀번호 로그인. */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest.Login request) {
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

    /** 리프레시 토큰으로 액세스/리프레시 토큰 재발급. */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody AuthRequest.RefreshToken request) {
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
    public ResponseEntity<AuthResponse> updatePreferences(
        Authentication authentication,
        @Valid @RequestBody AuthRequest.UpdatePreferences request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(authService.updatePreferences(userId, request));
    }

    /* ---------- (개발용) 전체 회원 목록 ---------- */
    // TODO(security): 아래 /users, /users/{userId} 엔드포인트는 현재 권한 체크가 없어
    //   로그인한 일반 사용자도 다른 사용자를 조회/삭제할 수 있음.
    //   운영 배포 전에 ADMIN 권한 체크를 추가하거나 엔드포인트 자체를 제거할 것.

    /** (개발용) 전체 사용자 목록 조회. */
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> listUsers() {
        return ResponseEntity.ok(
            userRepository.findAll().stream().map(UserDto::from).toList()
        );
    }

    /** (개발용) 특정 사용자 삭제. */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("USER_NOT_FOUND");
        }
        userRepository.deleteById(userId);
        return ResponseEntity.ok(Map.of(
            "deleted", true,
            "userId", userId
        ));
    }
}
