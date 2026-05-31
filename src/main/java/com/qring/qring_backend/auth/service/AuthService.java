package com.qring.qring_backend.auth.service;

import com.qring.qring_backend.auth.dto.AuthRequest;
import com.qring.qring_backend.auth.dto.AuthResponse;
import com.qring.qring_backend.auth.dto.LoginResponse;
import com.qring.qring_backend.auth.dto.SignUpResponse;
import com.qring.qring_backend.auth.dto.VerifyEmailResponse;
import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.auth.security.JwtTokenProvider;
import com.qring.qring_backend.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/** 인증 도메인의 핵심 비즈니스 로직 (가입·로그인·소셜·토큰 재발급·학습 설정). */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;
    private final OAuthService oAuthService;
    private final DisposableEmailService disposableEmailService;

    /**
     * Step 1: 미인증 상태로 저장 + 인증 코드 발송.
     * 이미 존재하지만 이메일 미인증 상태면 기존 레코드를 덮어씌운다.
     */
    @Transactional
    public SignUpResponse signUp(AuthRequest.SignUp request) {
        if (disposableEmailService.isDisposable(request.getEmail())) {
            throw new IllegalArgumentException("DISPOSABLE_EMAIL_NOT_ALLOWED");
        }

        userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            if (Boolean.TRUE.equals(existing.getEmailVerified())) {
                throw new IllegalArgumentException("EMAIL_ALREADY_EXISTS");
            }
            userRepository.delete(existing);
            userRepository.flush();
        });

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("NICKNAME_ALREADY_EXISTS");
        }

        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .nickname(request.getNickname())
            .authProvider("LOCAL")
            .emailVerified(false)
            .language(request.getLanguage())
            .levelCode(request.getLevelCode())
            .build();

        userRepository.save(user);

        boolean emailSent = false;
        String message;
        try {
            emailService.sendVerificationCode(request.getEmail());
            emailSent = true;
            message = "인증 코드를 이메일로 전송했습니다. 코드를 입력해 가입을 완료해 주세요.";
        } catch (Exception e) {
            log.warn("Verification email send failed for {}: {}", request.getEmail(), e.getMessage());
            message = "회원 정보는 저장되었지만 인증 메일 발송에 실패했습니다. 잠시 후 코드 재발송을 시도해 주세요.";
        }

        return new SignUpResponse(message, emailSent);
    }

    /** Step 2: 이메일 인증 코드 검증 후 토큰 발급. 응답은 토큰 + success만 포함. */
    @Transactional
    public VerifyEmailResponse verifyEmail(AuthRequest.VerifyEmail request) {
        EmailService.VerifyResult result =
            emailService.verifyCode(request.getEmail(), request.getCode());
        switch (result) {
            case NOT_FOUND -> throw new IllegalArgumentException("CODE_NOT_FOUND_OR_EXPIRED");
            case EXPIRED  -> throw new IllegalArgumentException("CODE_EXPIRED");
            case MISMATCH -> throw new IllegalArgumentException("CODE_MISMATCH");
            case OK       -> {}
        }
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
        user.setEmailVerified(true);
        userRepository.save(user);
        String at = tokenProvider.generateAccessToken(user.getUserId());
        String rt = tokenProvider.generateRefreshToken(user.getUserId());
        return new VerifyEmailResponse(at, rt, true);
    }

    /** 미인증 사용자용 코드 재발송. */
    public void resendCode(AuthRequest.ResendCode request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalArgumentException("EMAIL_ALREADY_VERIFIED");
        }
        emailService.sendVerificationCode(request.getEmail());
    }

    /** 이메일·비밀번호 검증과 이메일 인증 완료 여부 확인 후 토큰 페어 발급. */
    public LoginResponse login(AuthRequest.Login request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("INVALID_CREDENTIALS"));

        if (!"LOCAL".equals(user.getAuthProvider())) {
            throw new IllegalArgumentException("SOCIAL_LOGIN_ACCOUNT");
        }
        if (user.getPassword() == null ||
            !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("INVALID_CREDENTIALS");
        }
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalArgumentException("EMAIL_NOT_VERIFIED");
        }
        String at = tokenProvider.generateAccessToken(user.getUserId());
        String rt = tokenProvider.generateRefreshToken(user.getUserId());
        return new LoginResponse(at, rt);
    }

    /* -------------------------- 소셜 로그인 -------------------------- */

    /** Google ID 토큰 검증 후 내부 사용자로 매핑/생성. */
    @Transactional
    public AuthResponse googleLogin(AuthRequest.SocialLogin request) {
        Map<String, Object> u = oAuthService.verifyGoogleToken(request.getToken());
        return socialLogin(
            "GOOGLE",
            (String) u.get("sub"),
            (String) u.get("email"),
            (String) u.get("name")
        );
    }

    /** Kakao 인가 코드 → 사용자 프로필 → 내부 사용자로 매핑/생성. */
    @Transactional
    @SuppressWarnings("unchecked")
    public AuthResponse kakaoLogin(AuthRequest.SocialLogin request) {
        Map<String, Object> u = oAuthService.verifyKakaoCode(request.getToken(), request.getRedirectUri());
        String socialId = String.valueOf(u.get("id"));
        Map<String, Object> account = (Map<String, Object>) u.get("kakao_account");
        Map<String, Object> profile = account != null ? (Map<String, Object>) account.get("profile") : null;
        String email = account != null ? (String) account.get("email") : null;
        String nickname = profile != null ? (String) profile.get("nickname") : null;
        return socialLogin("KAKAO", socialId, email, nickname);
    }

    /** LINE 인가 코드 → 사용자 프로필 → 내부 사용자로 매핑/생성. */
    @Transactional
    public AuthResponse lineLogin(AuthRequest.SocialLogin request) {
        Map<String, Object> p = oAuthService.verifyLineCode(request.getToken(), request.getRedirectUri());
        return socialLogin("LINE", (String) p.get("userId"), null, (String) p.get("displayName"));
    }

    /**
     * 소셜 사용자 매핑 공통 로직: (provider, socialId) 매칭 → 이메일 충돌 검사 → 신규 생성.
     * 호출자(googleLogin/kakaoLogin/lineLogin)의 트랜잭션 안에서 동작한다.
     */
    private AuthResponse socialLogin(String provider, String socialId, String email, String name) {
        var existing = userRepository.findByAuthProviderAndSocialId(provider, socialId);
        if (existing.isPresent()) {
            return buildAuthResponse(existing.get());
        }

        if (email != null) {
            var byEmail = userRepository.findByEmail(email);
            if (byEmail.isPresent()) {
                String other = byEmail.get().getAuthProvider();
                throw new IllegalArgumentException(
                    "EMAIL_ALREADY_USED_BY_" + other);
            }
        }

        String base = (name != null && !name.isBlank()) ? sanitizeNickname(name) : "user";
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByNickname(candidate)) {
            candidate = base + suffix++;
        }
        String resolvedEmail = email != null
            ? email
            : socialId + "@" + provider.toLowerCase() + ".qring.local";
        User created = userRepository.save(User.builder()
            .email(resolvedEmail)
            .nickname(candidate)
            .authProvider(provider)
            .socialId(socialId)
            .emailVerified(true)
            .build());
        return buildAuthResponse(created);
    }

    /** 닉네임 후보 정규화 (허용 문자 외 제거 + 길이 보정). */
    private String sanitizeNickname(String raw) {
        String s = raw.replaceAll("[^a-zA-Z0-9가-힣_]", "");
        if (s.length() < 2) s = "user" + s;
        if (s.length() > 16) s = s.substring(0, 16);
        return s;
    }

    /** 리프레시 토큰 검증 후 동일 사용자에 대해 새 토큰 페어 발급. */
    public LoginResponse refresh(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("INVALID_REFRESH_TOKEN");
        }
        Long userId = tokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
        String at = tokenProvider.generateAccessToken(user.getUserId());
        String rt = tokenProvider.generateRefreshToken(user.getUserId());
        return new LoginResponse(at, rt);
    }

    /** 학습 설정 업데이트 (소셜 가입 후 OnboardingScreen에서 호출). */
    @Transactional
    public void updatePreferences(Long userId, AuthRequest.UpdatePreferences request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
        user.setLanguage(request.getLanguage());
        user.setLevelCode(request.getLevelCode());
        userRepository.save(user);
    }

    /** OAuth 응답 조립: 새 토큰 페어 + onboarding 필요 여부(language/levelCode null이면 true). */
    private AuthResponse buildAuthResponse(User user) {
        String at = tokenProvider.generateAccessToken(user.getUserId());
        String rt = tokenProvider.generateRefreshToken(user.getUserId());
        boolean isNewUser = user.getLanguage() == null || user.getLevelCode() == null;
        return new AuthResponse(at, rt, isNewUser);
    }
}
