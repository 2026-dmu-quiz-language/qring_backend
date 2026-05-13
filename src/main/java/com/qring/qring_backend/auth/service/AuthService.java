package com.qring.qring_backend.auth.service;

import com.qring.qring_backend.auth.dto.AuthRequest;
import com.qring.qring_backend.auth.dto.AuthResponse;
import com.qring.qring_backend.auth.dto.SignUpResponse;
import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.auth.security.JwtTokenProvider;
import com.qring.qring_backend.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

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
            .storyNickname(request.getStoryNickname())
            .authProvider("LOCAL")
            .emailVerified(false)
            .language(request.getLanguage())
            .levelCode(request.getLevelCode())
            .build();

        User saved = userRepository.save(user);
        emailService.sendVerificationCode(request.getEmail());

        return new SignUpResponse(
            saved.getUserId(),
            "PENDING_VERIFICATION",
            "인증 코드를 이메일로 전송했습니다. 코드를 입력해 가입을 완료해 주세요."
        );
    }

    /** Step 2: 이메일 인증 코드 검증 후 토큰 발급 */
    @Transactional
    public AuthResponse verifyEmail(AuthRequest.VerifyEmail request) {
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
        return buildAuthResponse(user, false);
    }

    /** 미인증 사용자용 코드 재발송 */
    public void resendCode(AuthRequest.ResendCode request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalArgumentException("EMAIL_ALREADY_VERIFIED");
        }
        emailService.sendVerificationCode(request.getEmail());
    }

    public AuthResponse login(AuthRequest.Login request) {
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
        return buildAuthResponse(user, false);
    }

    /* -------------------------- 소셜 로그인 -------------------------- */

    public AuthResponse googleLogin(AuthRequest.SocialLogin request) {
        Map<String, Object> u = oAuthService.verifyGoogleToken(request.getToken());
        return socialLogin(
            "GOOGLE",
            (String) u.get("sub"),
            (String) u.get("email"),
            (String) u.get("name")
        );
    }

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

    public AuthResponse lineLogin(AuthRequest.SocialLogin request) {
        Map<String, Object> p = oAuthService.verifyLineCode(request.getToken(), request.getRedirectUri());
        return socialLogin("LINE", (String) p.get("userId"), null, (String) p.get("displayName"));
    }

    @Transactional
    protected AuthResponse socialLogin(String provider, String socialId, String email, String name) {
        var existing = userRepository.findByAuthProviderAndSocialId(provider, socialId);
        if (existing.isPresent()) {
            return buildAuthResponse(existing.get(), false);
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
        return buildAuthResponse(created, true);
    }

    private String sanitizeNickname(String raw) {
        String s = raw.replaceAll("[^a-zA-Z0-9가-힣_]", "");
        if (s.length() < 2) s = "user" + s;
        if (s.length() > 16) s = s.substring(0, 16);
        return s;
    }

    public AuthResponse refresh(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("INVALID_REFRESH_TOKEN");
        }
        Long userId = tokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
        return buildAuthResponse(user, false);
    }

    /** 학습 설정 업데이트 (소셜 가입 후 OnboardingScreen에서 호출) */
    @Transactional
    public AuthResponse updatePreferences(Long userId, AuthRequest.UpdatePreferences request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
        user.setLanguage(request.getLanguage());
        user.setLevelCode(request.getLevelCode());
        userRepository.save(user);
        return buildAuthResponse(user, false);
    }

    private AuthResponse buildAuthResponse(User user, boolean isNewUser) {
        String at = tokenProvider.generateAccessToken(user.getUserId());
        String rt = tokenProvider.generateRefreshToken(user.getUserId());
        return new AuthResponse(
            at, rt,
            user.getUserId(),
            user.getEmail(),
            user.getNickname(),
            user.getAuthProvider(),
            isNewUser,
            user.getLanguage(),
            user.getLevelCode()
        );
    }
}
