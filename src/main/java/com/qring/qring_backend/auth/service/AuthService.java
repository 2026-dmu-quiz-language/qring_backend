package com.qring.qring_backend.auth.service;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.qring.qring_backend.auth.dto.AuthRequest;
import com.qring.qring_backend.auth.dto.AuthResponse;
import com.qring.qring_backend.auth.dto.LoginResponse;
import com.qring.qring_backend.auth.dto.SignUpResponse;
import com.qring.qring_backend.auth.dto.VerifyEmailResponse;
import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.auth.security.JwtTokenProvider;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.domain.user.UserLanguageLevel;
import com.qring.qring_backend.domain.user.UserLanguageLevelRepository;
import com.qring.qring_backend.domain.user.UserAsset;
import com.qring.qring_backend.domain.user.UserAssetRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final UserLanguageLevelRepository userLanguageLevelRepository;
    private final UserAssetRepository userAssetRepository;
    private final TransactionTemplate transactionTemplate;

    /**
     * Step 1: 미인증 상태로 저장 + 인증 코드 발송.
     * 이미 존재하지만 이메일 미인증 상태면 기존 레코드를 덮어씌운다.
     *
     * DB 저장은 짧은 트랜잭션으로 먼저 커밋하고, 메일 발송(수 초 소요)은 트랜잭션 밖에서 수행한다.
     * 메일 발송이 트랜잭션 안에 있으면 동일 이메일의 동시 요청이 유니크 제약 충돌(500)로 실패한다.
     */
    public SignUpResponse signUp(AuthRequest.SignUp request) {
        // 일회용(임시) 이메일은 가입 차단
        if (disposableEmailService.isDisposable(request.getEmail())) {
            throw new IllegalArgumentException("DISPOSABLE_EMAIL_NOT_ALLOWED");
        }

        try {
            transactionTemplate.executeWithoutResult(status -> registerPendingUser(request));
        } catch (DataIntegrityViolationException e) {
            // 조회~INSERT 사이에 다른 요청이 같은 이메일/닉네임을 먼저 넣은 경우 (동시 가입 경합)
            throw new IllegalArgumentException(resolveDuplicateCode(request, e));
        }

        // 인증 코드 이메일 발송 (실패해도 가입 정보는 유지하고 재발송 안내)
        boolean emailSent = false;
        String message;
        try {
            emailService.sendVerificationCode(request.getEmail());
            emailSent = true;
            message = "인증 코드를 이메일로 전송했습니다. 코드를 입력해 가입을 완료해 주세요.";
        } catch (IllegalArgumentException e) {
            if ("CODE_RESEND_COOLDOWN".equals(e.getMessage())) {
                // 미인증 계정을 지우고 60초 안에 다시 가입한 경우: 직전에 보낸 코드가 아직 유효하다
                emailSent = true;
                message = "최근에 보낸 인증 코드가 아직 유효합니다. 그 코드를 입력해 가입을 완료해 주세요.";
            } else {
                log.warn("Verification email send failed for {}: {}", request.getEmail(), e.getMessage());
                message = "회원 정보는 저장되었지만 인증 메일 발송에 실패했습니다. 잠시 후 코드 재발송을 시도해 주세요.";
            }
        } catch (Exception e) {
            log.warn("Verification email send failed for {}: {}", request.getEmail(), e.getMessage());
            message = "회원 정보는 저장되었지만 인증 메일 발송에 실패했습니다. 잠시 후 코드 재발송을 시도해 주세요.";
        }

        return new SignUpResponse(message, emailSent);
    }

    /** 가입 정보 저장 (트랜잭션 내부): 기존 미인증 계정 정리 → 닉네임 중복 검사 → 미인증 유저 INSERT. */
    private void registerPendingUser(AuthRequest.SignUp request) {
        // 같은 이메일이 있으면: 인증 완료 계정은 거부, 미인증 계정은 삭제 후 재가입 허용
        userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            if (Boolean.TRUE.equals(existing.getEmailVerified())) {
                throw new IllegalArgumentException("EMAIL_ALREADY_EXISTS");
            }
            userRepository.delete(existing);
            userRepository.flush();
        });

        // 닉네임 중복 검사
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("NICKNAME_ALREADY_EXISTS");
        }

        // 비밀번호는 암호화(BCrypt)해서 미인증 상태로 저장
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
    }

    /** 유니크 제약 위반 메시지에서 어느 값이 충돌했는지 판별해 도메인 오류 코드로 변환. */
    private String resolveDuplicateCode(AuthRequest.SignUp request, DataIntegrityViolationException e) {
        String msg = e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : "";
        if (msg != null && request.getNickname() != null && msg.contains("'" + request.getNickname() + "'")) {
            return "NICKNAME_ALREADY_EXISTS";
        }
        return "EMAIL_ALREADY_EXISTS";
    }

    /** Step 2: 이메일 인증 코드 검증 후 토큰 발급. 응답은 토큰 + success만 포함. */
    @Transactional
    public VerifyEmailResponse verifyEmail(AuthRequest.VerifyEmail request) {
        EmailService.throwIfNotOk(emailService.verifyCode(request.getEmail(), request.getCode()));
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
        user.setEmailVerified(true);
        userRepository.save(user);

        // 가입 완료(이메일 인증) 시점에 user_language_level 시드 row 생성 및 초기 자산(50포인트) 설정
        seedUserLanguageLevel(user);
        initUserAsset(user);

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
        // 이메일로 사용자 조회 (없으면 자격 증명 오류로 통일)
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("INVALID_CREDENTIALS"));

        // 소셜 가입 계정은 비밀번호 로그인 불가
        if (!"LOCAL".equals(user.getAuthProvider())) {
            throw new IllegalArgumentException("SOCIAL_LOGIN_ACCOUNT");
        }
        // 비밀번호 일치 검증 (암호화된 값과 비교)
        if (user.getPassword() == null ||
            !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("INVALID_CREDENTIALS");
        }
        // 이메일 인증을 마친 계정만 로그인 허용
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalArgumentException("EMAIL_NOT_VERIFIED");
        }
        // 검증 통과 시 액세스/리프레시 토큰 발급
        String at = tokenProvider.generateAccessToken(user.getUserId());
        String rt = tokenProvider.generateRefreshToken(user.getUserId());
        return new LoginResponse(at, rt);
    }

    /* -------------------------- 비밀번호 찾기 (PASSWORD_RESET_DESIGN.md) -------------------------- */

    /**
     * 1단계: 재설정 코드 발송. LOCAL 이면서 이메일 인증을 마친 계정에만 보낸다.
     * 없는 이메일은 USER_NOT_FOUND 를 명시한다 (check-email 이 이미 가입 여부를 공개하므로 숨길 실익이 없음 — 팀 결정).
     */
    public void forgotPassword(AuthRequest.ForgotPassword request) {
        User user = findLocalVerifiedUser(request.getEmail());
        emailService.sendCode(user.getEmail(), EmailService.Purpose.PASSWORD_RESET);
    }

    /**
     * 2단계: 재설정 코드 검증 → 재설정 토큰(10분, 1회용) 발급. 코드는 검증과 함께 소멸한다.
     * 발급한 토큰의 서명은 사용자별로 기억해 두었다가 3단계에서 한 번만 쓰이게 한다.
     */
    public String verifyResetCode(AuthRequest.VerifyResetCode request) {
        User user = findLocalVerifiedUser(request.getEmail());
        EmailService.throwIfNotOk(
            emailService.verifyCode(user.getEmail(), request.getCode(), EmailService.Purpose.PASSWORD_RESET));
        String token = tokenProvider.generateResetToken(user.getUserId());
        issuedResetTokens.put(user.getUserId(), signatureOf(token));
        return token;
    }

    /**
     * 3단계: 재설정 토큰 검증 후 새 비밀번호 저장. 기존 비밀번호와 같아도 허용한다 (팀 결정).
     * 토큰은 서명·만료·타입(reset)·1회용 여부를 모두 확인한다.
     */
    @Transactional
    public void resetPassword(AuthRequest.ResetPassword request) {
        String token = request.getResetToken();
        if (!tokenProvider.validateToken(token)) {
            // 서명 오류와 만료를 구분해 안내한다 (만료면 "다시 요청" 안내)
            throw new IllegalArgumentException(tokenProvider.isExpired(token) ? "RESET_TOKEN_EXPIRED" : "INVALID_RESET_TOKEN");
        }
        if (!JwtTokenProvider.TYPE_RESET.equals(tokenProvider.getTokenType(token))) {
            throw new IllegalArgumentException("INVALID_RESET_TOKEN");
        }
        Long userId = tokenProvider.getUserIdFromToken(token);
        String issued = issuedResetTokens.get(userId);
        if (issued == null || !issued.equals(signatureOf(token))) {
            // 이미 사용됐거나(1회용), 서버 재시작 등으로 발급 기록이 없는 토큰
            throw new IllegalArgumentException("RESET_TOKEN_EXPIRED");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
        if (!"LOCAL".equals(user.getAuthProvider())) {
            throw new IllegalArgumentException("SOCIAL_LOGIN_ACCOUNT");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        issuedResetTokens.remove(userId);
        log.info("[PasswordReset] password reset completed for userId {}", userId);
    }

    /** 재설정 대상 계정 판정: 존재해야 하고, LOCAL 이어야 하고, 이메일 인증을 마쳐야 한다. */
    private User findLocalVerifiedUser(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
        if (!"LOCAL".equals(user.getAuthProvider())) {
            throw new IllegalArgumentException("SOCIAL_LOGIN_ACCOUNT");
        }
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalArgumentException("EMAIL_NOT_VERIFIED");
        }
        return user;
    }

    /** userId → 마지막으로 발급한 재설정 토큰의 서명. 사용하면 지운다 (1회용). 가입 인증 코드처럼 서버 메모리에 둔다. */
    private final java.util.concurrent.ConcurrentHashMap<Long, String> issuedResetTokens =
        new java.util.concurrent.ConcurrentHashMap<>();

    /** 회원 탈퇴 시 메모리에 남은 재설정 토큰 서명을 지운다 (UserWithdrawalService 에서 호출). */
    public void discardResetToken(Long userId) {
        issuedResetTokens.remove(userId);
    }

    private static String signatureOf(String jwt) {
        int dot = jwt.lastIndexOf('.');
        return dot < 0 ? jwt : jwt.substring(dot + 1);
    }

    /* -------------------------- 소셜 로그인 -------------------------- */

    /** Google ID 토큰 검증 후 내부 사용자로 매핑/생성. */
    @Transactional
    public AuthResponse googleLogin(AuthRequest.SocialLogin request) {
        Map<String, Object> u = oAuthService.verifyGoogleToken(request.getToken());
        return socialLogin("GOOGLE", (String) u.get("sub"), (String) u.get("email"));
    }

    /** Kakao 인가 코드 → 사용자 프로필 → 내부 사용자로 매핑/생성. */
    @Transactional
    @SuppressWarnings("unchecked")
    public AuthResponse kakaoLogin(AuthRequest.SocialLogin request) {
        Map<String, Object> u = oAuthService.verifyKakaoCode(request.getToken(), request.getRedirectUri());
        String socialId = String.valueOf(u.get("id"));
        Map<String, Object> account = (Map<String, Object>) u.get("kakao_account");
        String email = account != null ? (String) account.get("email") : null;
        return socialLogin("KAKAO", socialId, email);
    }

    /** LINE 인가 코드 → 사용자 프로필 → 내부 사용자로 매핑/생성. */
    @Transactional
    public AuthResponse lineLogin(AuthRequest.SocialLogin request) {
        Map<String, Object> p = oAuthService.verifyLineCode(request.getToken(), request.getRedirectUri());
        return socialLogin("LINE", (String) p.get("userId"), null);
    }

    /**
     * 소셜 사용자 매핑 공통 로직: (provider, socialId) 매칭 → 이메일 충돌 검사 → 신규 생성.
     * 호출자(googleLogin/kakaoLogin/lineLogin)의 트랜잭션 안에서 동작한다.
     */
    private AuthResponse socialLogin(String provider, String socialId, String email) {
        var existing = userRepository.findByAuthProviderAndSocialId(provider, socialId);
        if (existing.isPresent()) {
            return buildAuthResponse(existing.get());
        }

        if (email != null) {
            var byEmail = userRepository.findByEmail(email);
            if (byEmail.isPresent()) {
                throw new IllegalArgumentException("EMAIL_ALREADY_USED_BY_" + byEmail.get().getAuthProvider());
            }
        }

        String resolvedEmail = email != null
            ? email
            : socialId + "@" + provider.toLowerCase() + ".qring.local";

        User created = userRepository.save(User.builder()
            .email(resolvedEmail)
            .nickname(tempNickname(socialId))
            .authProvider(provider)
            .socialId(socialId)
            .emailVerified(true)
            .build());
            
        initUserAsset(created);
        return buildAuthResponse(created);
    }

    /** 온보딩에서 닉네임을 확정하기 전까지 쓸 임시 닉네임 생성 (user_ + socialId 뒷자리, 중복 시 숫자 접미사). */
    private String tempNickname(String socialId) {
        String tail = socialId.length() > 8 ? socialId.substring(socialId.length() - 8) : socialId;
        String base = "user_" + tail;
        if (!userRepository.existsByNickname(base)) return base;
        for (int i = 1; i < 1000; i++) {
            String candidate = base + i;
            if (!userRepository.existsByNickname(candidate)) return candidate;
        }
        throw new IllegalArgumentException("SOCIAL_LOGIN_FAILED");
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

    /** 학습 설정 업데이트 (소셜 가입 후 OnboardingScreen에서 호출). 닉네임이 오면 중복 검사 후 함께 확정한다. */
    @Transactional
    public void updatePreferences(Long userId, AuthRequest.UpdatePreferences request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));

        // 닉네임 확정: 본인 현재 닉네임과 같으면 그대로 두고, 타인이 사용 중이면 거부
        String nickname = request.getNickname();
        if (nickname != null && !nickname.equals(user.getNickname())) {
            if (userRepository.existsByNickname(nickname)) {
                throw new IllegalArgumentException("NICKNAME_ALREADY_EXISTS");
            }
            user.setNickname(nickname);
        }

        user.setLanguage(request.getLanguage());
        user.setLevelCode(request.getLevelCode());
        userRepository.save(user);

        // 온보딩 완료(언어/레벨 확정) 시점에 user_language_level 시드 row 생성
        seedUserLanguageLevel(user);
    }

    /** OAuth 응답 조립: 새 토큰 페어 + onboarding 필요 여부(language/levelCode null이면 true). */
    private AuthResponse buildAuthResponse(User user) {
        String at = tokenProvider.generateAccessToken(user.getUserId());
        String rt = tokenProvider.generateRefreshToken(user.getUserId());
        boolean isNewUser = user.getLanguage() == null || user.getLevelCode() == null;
        return new AuthResponse(at, rt, isNewUser);
    }

    /** user_language_level 시드 row 생성: 이미 해당 (user, language) row가 있으면 건너뜀. */
    private void seedUserLanguageLevel(User user) {
        if (user.getLanguage() == null || user.getLevelCode() == null) {
            return;
        }
        boolean exists = userLanguageLevelRepository
            .existsByUserIdAndLanguage(user.getUserId(), user.getLanguage());
        if (!exists) {
            UserLanguageLevel ull = new UserLanguageLevel();
            ull.setUserId(user.getUserId());
            ull.setLanguage(user.getLanguage());
            ull.setLevel(user.getLevelCode());
            userLanguageLevelRepository.save(ull);
        }
    }

    /** 신규 유저 가입 시 초기 자산(포인트 50점) 설정. 이미 자산이 있다면 건너뜀. */
    private void initUserAsset(User user) {
        if (userAssetRepository.findByUserUserId(user.getUserId()).isEmpty()) {
            UserAsset asset = new UserAsset();
            asset.setUser(user);
            asset.setCurrentPoints(50);
            asset.setTotalExp(0);
            asset.setStreakDays(0);
            userAssetRepository.save(asset);
        }
    }
}