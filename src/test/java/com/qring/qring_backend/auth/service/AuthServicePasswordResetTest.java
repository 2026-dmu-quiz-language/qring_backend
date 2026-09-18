package com.qring.qring_backend.auth.service;

import com.qring.qring_backend.auth.dto.AuthRequest;
import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.auth.security.JwtTokenProvider;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.domain.user.UserAssetRepository;
import com.qring.qring_backend.domain.user.UserLanguageLevelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** 비밀번호 찾기 3단계 흐름: 대상 계정 판정, 코드 → 재설정 토큰, 토큰 1회용, 새 비밀번호 저장. */
class AuthServicePasswordResetTest {

    private static final String EMAIL = "user@example.com";

    private UserRepository userRepository;
    private EmailService emailService;
    private JwtTokenProvider tokenProvider;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;
    private User localUser;

    @BeforeEach
    void setUp() throws Exception {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();

        emailService = new EmailService(null);
        emailService.setDevMode(true);

        tokenProvider = new JwtTokenProvider();
        set(tokenProvider, "secret", "qring-test-secret-key-please-change-in-production-min-32-bytes");
        set(tokenProvider, "accessTokenValidityMs", 3600000L);
        set(tokenProvider, "refreshTokenValidityMs", 1209600000L);
        set(tokenProvider, "resetTokenValidityMs", 600000L);
        tokenProvider.init();

        authService = new AuthService(userRepository, passwordEncoder, tokenProvider, emailService,
                mock(OAuthService.class), mock(DisposableEmailService.class),
                mock(UserLanguageLevelRepository.class), mock(UserAssetRepository.class),
                mock(TransactionTemplate.class));

        localUser = User.builder().userId(1L).email(EMAIL).password(passwordEncoder.encode("OldPass1!"))
                .nickname("tester").authProvider("LOCAL").emailVerified(true).build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(localUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(localUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static void set(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    private AuthRequest.ForgotPassword forgot(String email) {
        AuthRequest.ForgotPassword r = new AuthRequest.ForgotPassword();
        r.setEmail(email);
        return r;
    }

    private AuthRequest.VerifyResetCode verify(String email, String code) {
        AuthRequest.VerifyResetCode r = new AuthRequest.VerifyResetCode();
        r.setEmail(email);
        r.setCode(code);
        return r;
    }

    private AuthRequest.ResetPassword reset(String token, String password) {
        AuthRequest.ResetPassword r = new AuthRequest.ResetPassword();
        r.setResetToken(token);
        r.setNewPassword(password);
        return r;
    }

    @Test
    @DisplayName("정상 흐름: 코드 발송 → 코드 검증으로 재설정 토큰 → 새 비밀번호 저장, 토큰은 1회용")
    void happyPath() {
        authService.forgotPassword(forgot(EMAIL));
        String code = emailService.peekCode(EMAIL, EmailService.Purpose.PASSWORD_RESET);
        assertNotNull(code, "재설정 코드가 발급되어야 한다");

        String token = authService.verifyResetCode(verify(EMAIL, code));
        assertEquals(JwtTokenProvider.TYPE_RESET, tokenProvider.getTokenType(token));

        authService.resetPassword(reset(token, "NewPass1!"));
        assertTrue(passwordEncoder.matches("NewPass1!", localUser.getPassword()), "새 비밀번호가 암호화 저장된다");

        IllegalArgumentException again = assertThrows(IllegalArgumentException.class,
                () -> authService.resetPassword(reset(token, "Another1!")));
        assertEquals("RESET_TOKEN_EXPIRED", again.getMessage(), "같은 토큰을 두 번 쓸 수 없다");
    }

    @Test
    @DisplayName("기존 비밀번호와 같은 값도 허용한다 (팀 결정)")
    void samePasswordIsAllowed() {
        authService.forgotPassword(forgot(EMAIL));
        String token = authService.verifyResetCode(verify(EMAIL, emailService.peekCode(EMAIL, EmailService.Purpose.PASSWORD_RESET)));
        authService.resetPassword(reset(token, "OldPass1!"));
        assertTrue(passwordEncoder.matches("OldPass1!", localUser.getPassword()));
    }

    @Test
    @DisplayName("없는 이메일 / 소셜 계정 / 미인증 계정은 코드를 보내지 않는다")
    void rejectsIneligibleAccounts() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());
        assertEquals("USER_NOT_FOUND", assertThrows(IllegalArgumentException.class,
                () -> authService.forgotPassword(forgot("nobody@example.com"))).getMessage());

        User social = User.builder().userId(2L).email("g@example.com").authProvider("GOOGLE").emailVerified(true).build();
        when(userRepository.findByEmail("g@example.com")).thenReturn(Optional.of(social));
        assertEquals("SOCIAL_LOGIN_ACCOUNT", assertThrows(IllegalArgumentException.class,
                () -> authService.forgotPassword(forgot("g@example.com"))).getMessage());

        User unverified = User.builder().userId(3L).email("u@example.com").authProvider("LOCAL").emailVerified(false).build();
        when(userRepository.findByEmail("u@example.com")).thenReturn(Optional.of(unverified));
        assertEquals("EMAIL_NOT_VERIFIED", assertThrows(IllegalArgumentException.class,
                () -> authService.forgotPassword(forgot("u@example.com"))).getMessage());
    }

    @Test
    @DisplayName("코드가 틀리면 CODE_MISMATCH, 가입 인증 코드로는 재설정할 수 없다")
    void wrongOrForeignCode() {
        authService.forgotPassword(forgot(EMAIL));
        String code = emailService.peekCode(EMAIL, EmailService.Purpose.PASSWORD_RESET);
        String wrong = code.equals("000000") ? "111111" : "000000";
        assertEquals("CODE_MISMATCH", assertThrows(IllegalArgumentException.class,
                () -> authService.verifyResetCode(verify(EMAIL, wrong))).getMessage());

        emailService.sendVerificationCode(EMAIL);
        String signupCode = emailService.peekCode(EMAIL, EmailService.Purpose.SIGNUP);
        if (!signupCode.equals(code)) {
            assertEquals("CODE_MISMATCH", assertThrows(IllegalArgumentException.class,
                    () -> authService.verifyResetCode(verify(EMAIL, signupCode))).getMessage());
        }
    }

    @Test
    @DisplayName("액세스 토큰이나 위조 토큰으로는 비밀번호를 바꿀 수 없다")
    void rejectsNonResetTokens() {
        String access = tokenProvider.generateAccessToken(1L);
        assertEquals("INVALID_RESET_TOKEN", assertThrows(IllegalArgumentException.class,
                () -> authService.resetPassword(reset(access, "NewPass1!"))).getMessage());
        assertEquals("INVALID_RESET_TOKEN", assertThrows(IllegalArgumentException.class,
                () -> authService.resetPassword(reset("garbage.token.value", "NewPass1!"))).getMessage());

        // 발급 기록이 없는(서버 재시작 등) 정상 서명 토큰도 거부
        String orphan = tokenProvider.generateResetToken(1L);
        assertEquals("RESET_TOKEN_EXPIRED", assertThrows(IllegalArgumentException.class,
                () -> authService.resetPassword(reset(orphan, "NewPass1!"))).getMessage());
    }
}
