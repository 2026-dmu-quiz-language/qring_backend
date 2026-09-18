package com.qring.qring_backend.auth.service;

import com.qring.qring_backend.auth.service.EmailService.Purpose;
import com.qring.qring_backend.auth.service.EmailService.VerifyResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** 인증 코드 저장소: 용도 분리, 재발송 쿨다운, 오답 횟수 제한. (개발 모드라 메일은 보내지 않는다) */
class EmailServiceTest {

    private static final String EMAIL = "user@example.com";

    private EmailService service;
    private Instant now;

    @BeforeEach
    void setUp() {
        service = new EmailService(null);
        service.setDevMode(true);
        now = Instant.parse("2026-09-18T10:00:00Z");
        service.setClock(clockAt(now));
    }

    private Clock clockAt(Instant instant) {
        return Clock.fixed(instant, ZoneOffset.UTC);
    }

    private void advance(Duration d) {
        now = now.plus(d);
        service.setClock(clockAt(now));
    }

    @Test
    @DisplayName("가입 코드로 비밀번호 재설정을 검증할 수 없다 (용도별 저장)")
    void codesAreSeparatedByPurpose() {
        service.sendCode(EMAIL, Purpose.SIGNUP);
        String signupCode = service.peekCode(EMAIL, Purpose.SIGNUP);
        assertNotNull(signupCode);

        assertEquals(VerifyResult.NOT_FOUND, service.verifyCode(EMAIL, signupCode, Purpose.PASSWORD_RESET));
        assertEquals(VerifyResult.OK, service.verifyCode(EMAIL, signupCode, Purpose.SIGNUP));
        assertNull(service.peekCode(EMAIL, Purpose.SIGNUP), "성공하면 1회용으로 사라진다");
    }

    @Test
    @DisplayName("기존 호출부(sendVerificationCode/verifyCode)는 가입 용도로 그대로 동작한다")
    void legacySignupApiStillWorks() {
        service.sendVerificationCode(EMAIL);
        String code = service.peekCode(EMAIL, Purpose.SIGNUP);
        assertEquals(VerifyResult.OK, service.verifyCode(EMAIL, code));
    }

    @Test
    @DisplayName("같은 이메일·용도로 60초 안에 다시 요청하면 거부하고, 60초가 지나면 새 코드를 낸다")
    void resendCooldown() {
        service.sendCode(EMAIL, Purpose.PASSWORD_RESET);
        String first = service.peekCode(EMAIL, Purpose.PASSWORD_RESET);

        advance(Duration.ofSeconds(30));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.sendCode(EMAIL, Purpose.PASSWORD_RESET));
        assertEquals("CODE_RESEND_COOLDOWN", e.getMessage());
        assertEquals(first, service.peekCode(EMAIL, Purpose.PASSWORD_RESET), "거부되면 기존 코드가 유지된다");

        // 다른 용도는 쿨다운에 걸리지 않는다
        service.sendCode(EMAIL, Purpose.SIGNUP);

        advance(Duration.ofSeconds(31));
        service.sendCode(EMAIL, Purpose.PASSWORD_RESET);
        assertNotNull(service.peekCode(EMAIL, Purpose.PASSWORD_RESET));
    }

    @Test
    @DisplayName("오답이 5회 쌓이면 코드를 폐기한다")
    void tooManyAttemptsDiscardsCode() {
        service.sendCode(EMAIL, Purpose.PASSWORD_RESET);
        String code = service.peekCode(EMAIL, Purpose.PASSWORD_RESET);
        String wrong = code.equals("000000") ? "111111" : "000000";

        for (int i = 1; i < EmailService.MAX_VERIFY_ATTEMPTS; i++) {
            assertEquals(VerifyResult.MISMATCH, service.verifyCode(EMAIL, wrong, Purpose.PASSWORD_RESET), "attempt " + i);
        }
        assertEquals(VerifyResult.TOO_MANY_ATTEMPTS, service.verifyCode(EMAIL, wrong, Purpose.PASSWORD_RESET));
        assertEquals(VerifyResult.NOT_FOUND, service.verifyCode(EMAIL, code, Purpose.PASSWORD_RESET),
                "폐기된 뒤에는 맞는 코드도 통하지 않는다");
    }

    @Test
    @DisplayName("10분이 지나면 만료된다")
    void codeExpires() {
        service.sendCode(EMAIL, Purpose.PASSWORD_RESET);
        String code = service.peekCode(EMAIL, Purpose.PASSWORD_RESET);
        advance(Duration.ofMinutes(10).plusSeconds(1));
        assertEquals(VerifyResult.EXPIRED, service.verifyCode(EMAIL, code, Purpose.PASSWORD_RESET));
    }

    @Test
    @DisplayName("이메일 대소문자·공백 차이는 같은 키로 본다")
    void emailKeyIsNormalized() {
        service.sendCode("  User@Example.com ", Purpose.PASSWORD_RESET);
        String code = service.peekCode(EMAIL, Purpose.PASSWORD_RESET);
        assertNotNull(code);
        assertEquals(VerifyResult.OK, service.verifyCode("USER@EXAMPLE.COM", code, Purpose.PASSWORD_RESET));
    }

    @Test
    @DisplayName("결과를 도메인 오류 코드로 바꾼다")
    void mapsResultsToErrorCodes() {
        assertEquals("CODE_NOT_FOUND_OR_EXPIRED", assertThrows(IllegalArgumentException.class,
                () -> EmailService.throwIfNotOk(VerifyResult.NOT_FOUND)).getMessage());
        assertEquals("CODE_EXPIRED", assertThrows(IllegalArgumentException.class,
                () -> EmailService.throwIfNotOk(VerifyResult.EXPIRED)).getMessage());
        assertEquals("CODE_MISMATCH", assertThrows(IllegalArgumentException.class,
                () -> EmailService.throwIfNotOk(VerifyResult.MISMATCH)).getMessage());
        assertEquals("TOO_MANY_ATTEMPTS", assertThrows(IllegalArgumentException.class,
                () -> EmailService.throwIfNotOk(VerifyResult.TOO_MANY_ATTEMPTS)).getMessage());
        EmailService.throwIfNotOk(VerifyResult.OK);
    }
}
