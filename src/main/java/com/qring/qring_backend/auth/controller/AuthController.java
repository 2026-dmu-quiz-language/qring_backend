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

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    /* ---------- 로컬 회원가입 & 이메일 인증 ---------- */

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody AuthRequest.SignUp request) {
        return ResponseEntity.ok(authService.signUp(request));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(@Valid @RequestBody AuthRequest.VerifyEmail request) {
        return ResponseEntity.ok(authService.verifyEmail(request));
    }

    @PostMapping("/resend-code")
    public ResponseEntity<Map<String, String>> resendCode(@Valid @RequestBody AuthRequest.ResendCode request) {
        authService.resendCode(request);
        return ResponseEntity.ok(Map.of("message", "인증 코드가 재발송되었습니다."));
    }

    /* ---------- 로그인 / 로그아웃 / 리프레시 ---------- */

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest.Login request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        return ResponseEntity.ok(Map.of(
            "logoutStatus", "SUCCESS",
            "message", "로그아웃 되었습니다."
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody AuthRequest.RefreshToken request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    /* ---------- 소셜 로그인 ---------- */

    @PostMapping("/oauth/google")
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody AuthRequest.SocialLogin request) {
        return ResponseEntity.ok(authService.googleLogin(request));
    }

    @PostMapping("/oauth/kakao")
    public ResponseEntity<AuthResponse> kakaoLogin(@Valid @RequestBody AuthRequest.SocialLogin request) {
        return ResponseEntity.ok(authService.kakaoLogin(request));
    }

    @PostMapping("/oauth/line")
    public ResponseEntity<AuthResponse> lineLogin(@Valid @RequestBody AuthRequest.SocialLogin request) {
        return ResponseEntity.ok(authService.lineLogin(request));
    }

    /* ---------- 중복 체크 / 내 정보 ---------- */

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean available = userRepository.findByEmail(email)
            .map(u -> !Boolean.TRUE.equals(u.getEmailVerified()))
            .orElse(true);
        return ResponseEntity.ok(Map.of("available", available));
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Boolean>> checkNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(Map.of("available", !userRepository.existsByNickname(nickname)));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
        return ResponseEntity.ok(UserDto.from(user));
    }

    /* ---------- 학습 설정 (Onboarding) ---------- */

    @PutMapping("/preferences")
    public ResponseEntity<AuthResponse> updatePreferences(
        Authentication authentication,
        @Valid @RequestBody AuthRequest.UpdatePreferences request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(authService.updatePreferences(userId, request));
    }

    /* ---------- (개발용) 전체 회원 목록 ---------- */

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> listUsers() {
        return ResponseEntity.ok(
            userRepository.findAll().stream().map(UserDto::from).toList()
        );
    }

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
