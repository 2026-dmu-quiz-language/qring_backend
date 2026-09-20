package com.qring.qring_backend.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 이메일 도메인 검증: 형식·예약 도메인·임시메일·DNS MX 판정과 캐시 동작. 실제 DNS 대신 가짜 리졸버를 사용한다. */
class EmailDomainServiceTest {

    /** 도메인별 결과를 미리 정해 두고, 호출된 도메인을 기록하는 가짜 리졸버. */
    private static class FakeResolver implements DnsMxResolver {
        final Map<String, Result> answers = new HashMap<>();
        final List<String> calls = new ArrayList<>();

        @Override
        public Result lookupMx(String domain) {
            calls.add(domain);
            return answers.getOrDefault(domain, Result.NO_MX);
        }
    }

    private FakeResolver resolver;
    private EmailDomainService service;

    @BeforeEach
    void setUp() {
        resolver = new FakeResolver();
        service = new EmailDomainService(new DisposableEmailService(), resolver);
    }

    private String codeOf(String email) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> service.validateOrThrow(email));
        return e.getMessage();
    }

    @Test
    @DisplayName("MX 레코드가 있는 도메인은 통과한다")
    void hasMx_passes() {
        resolver.answers.put("naver.com", DnsMxResolver.Result.HAS_MX);
        assertDoesNotThrow(() -> service.validateOrThrow("user@naver.com"));
    }

    @Test
    @DisplayName("MX 레코드가 없으면 INVALID_EMAIL_DOMAIN (A/AAAA 폴백 없음)")
    void noMx_rejected() {
        resolver.answers.put("nomx-domain.com", DnsMxResolver.Result.NO_MX);
        assertEquals("INVALID_EMAIL_DOMAIN", codeOf("user@nomx-domain.com"));
    }

    @Test
    @DisplayName("DNS 조회 자체가 실패해도 거부한다 (fail-closed), 단 캐시하지 않아 다음 요청은 재조회된다")
    void lookupFailed_rejectedButNotCached() {
        resolver.answers.put("flaky.com", DnsMxResolver.Result.LOOKUP_FAILED);
        assertEquals("INVALID_EMAIL_DOMAIN", codeOf("user@flaky.com"));

        resolver.answers.put("flaky.com", DnsMxResolver.Result.HAS_MX);
        assertDoesNotThrow(() -> service.validateOrThrow("user@flaky.com"));
        assertEquals(2, resolver.calls.size());
    }

    @Test
    @DisplayName("확정 결과(HAS_MX/NO_MX)는 도메인 단위로 캐시되어 DNS 를 한 번만 조회한다")
    void definiteResults_cached() {
        resolver.answers.put("gmail.com", DnsMxResolver.Result.HAS_MX);
        resolver.answers.put("dead.com", DnsMxResolver.Result.NO_MX);

        service.validateOrThrow("a@gmail.com");
        service.validateOrThrow("b@gmail.com");
        service.validateOrThrow("c@GMAIL.com");
        codeOf("x@dead.com");
        codeOf("y@dead.com");

        assertEquals(List.of("gmail.com", "dead.com"), resolver.calls);
    }

    @Test
    @DisplayName("캐시 TTL 이 지나면 다시 조회한다")
    void cacheExpires() {
        service.setCacheTtlValid(Duration.ZERO);
        resolver.answers.put("gmail.com", DnsMxResolver.Result.HAS_MX);

        service.validateOrThrow("a@gmail.com");
        service.validateOrThrow("b@gmail.com");
        assertEquals(2, resolver.calls.size());
    }

    @Test
    @DisplayName("예약/테스트 도메인과 예약 TLD 는 DNS 조회 없이 즉시 거부한다")
    void reservedDomains_rejectedWithoutDns() {
        for (String email : List.of(
                "a@example.com", "a@mail.example.org", "a@test.com", "a@localhost",
                "a@foo.test", "a@foo.invalid", "a@printer.local", "a@svc.internal")) {
            assertEquals("INVALID_EMAIL_DOMAIN", codeOf(email), email);
        }
        assertEquals(0, resolver.calls.size());
    }

    @Test
    @DisplayName("형식이 잘못된 이메일은 DNS 조회 없이 INVALID_EMAIL_DOMAIN")
    void malformed_rejectedWithoutDns() {
        for (String email : List.of(
                "test@COM", "user@", "@naver.com", "no-at-sign", "user@naver", "user@-bad-.com",
                "user@na..ver.com", "user@naver.c0m", "")) {
            assertEquals("INVALID_EMAIL_DOMAIN", codeOf(email), email);
        }
        assertEquals("INVALID_EMAIL_DOMAIN", codeOf(null));
        assertEquals(0, resolver.calls.size());
    }

    @Test
    @DisplayName("임시메일 도메인은 DISPOSABLE_EMAIL_NOT_ALLOWED (DNS 조회 전에 판정)")
    void disposable_rejected() {
        assertEquals("DISPOSABLE_EMAIL_NOT_ALLOWED", codeOf("a@mailinator.com"));
        assertEquals(0, resolver.calls.size());
    }

    @Test
    @DisplayName("한글 도메인은 punycode 로 정규화해 조회한다")
    void idn_normalized() {
        resolver.answers.put("xn--3e0b707e.kr", DnsMxResolver.Result.HAS_MX);   // 한국.kr
        assertDoesNotThrow(() -> service.validateOrThrow("user@한국.kr"));
        assertEquals(List.of("xn--3e0b707e.kr"), resolver.calls);
    }

    @Test
    @DisplayName("normalizeDomain: 소문자·공백 제거·끝 점 제거, 잘못된 구조는 null")
    void normalizeDomain() {
        assertEquals("naver.com", EmailDomainService.normalizeDomain("  User@Naver.COM.  "));
        assertEquals("mail.co.kr", EmailDomainService.normalizeDomain("a@mail.co.kr"));
        assertNull(EmailDomainService.normalizeDomain("user@COM"));
        assertNull(EmailDomainService.normalizeDomain("user@1.2.3.4"));
    }

    @Test
    @DisplayName("isAllowed: check-email 용 boolean 판정 (예외 없이 true/false)")
    void isAllowed_booleanVerdict() {
        resolver.answers.put("naver.com", DnsMxResolver.Result.HAS_MX);
        assertTrue(service.isAllowed("user@naver.com"));
        assertFalse(service.isAllowed("user@example.com"));
        assertFalse(service.isAllowed("user@mailinator.com"));
        assertFalse(service.isAllowed("user@nomx.com"));
        assertFalse(service.isAllowed("broken"));
    }

    @Test
    @DisplayName("dns-check 비활성화 시 형식·예약·임시메일 검사만 하고 DNS 는 건너뛴다")
    void disabled_skipsDns() {
        service.setDnsCheckEnabled(false);
        assertDoesNotThrow(() -> service.validateOrThrow("user@unknown-domain.com"));
        assertEquals("INVALID_EMAIL_DOMAIN", codeOf("user@example.com"));
        assertEquals(0, resolver.calls.size());
    }
}
