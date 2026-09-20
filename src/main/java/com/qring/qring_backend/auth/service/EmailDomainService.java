package com.qring.qring_backend.auth.service;

import java.net.IDN;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 가입용 이메일 도메인 검증: 형식 → 예약/테스트 도메인 → 임시메일 → DNS MX 조회 순.
 * 회원가입과 check-email 에서만 호출한다 (로그인·코드 재발송·비밀번호 찾기·소셜 로그인은 대상 아님).
 *
 * 팀 결정(2026-09-20): MX 없는 도메인은 A/AAAA 폴백 없이 거부, DNS 조회 실패도 거부(fail-closed).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailDomainService {

    public static final String INVALID_EMAIL_DOMAIN = "INVALID_EMAIL_DOMAIN";
    public static final String DISPOSABLE_EMAIL_NOT_ALLOWED = "DISPOSABLE_EMAIL_NOT_ALLOWED";

    /** punycode 변환 후 도메인 구조 검사: 각 라벨 1~63자, 전체 253자 이하, TLD 는 영문 2자 이상. */
    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
        "^(?=.{1,253}$)(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,63}$");

    /** 문서·테스트용으로 예약된 도메인 (서브도메인 포함 거부). */
    private static final Set<String> RESERVED_DOMAINS = Set.of(
        "example.com", "example.net", "example.org", "test.com", "localhost"
    );

    /** 공인 인터넷에 존재하지 않는 예약 TLD. */
    private static final Set<String> RESERVED_TLDS = Set.of(
        "test", "example", "invalid", "localhost", "local", "internal"
    );

    private final DisposableEmailService disposableEmailService;
    private final DnsMxResolver dnsMxResolver;

    @Setter
    @Value("${qring.email.dns-check.enabled:true}")
    private boolean dnsCheckEnabled = true;

    @Setter
    @Value("${qring.email.dns-check.cache-ttl-valid:24h}")
    private Duration cacheTtlValid = Duration.ofHours(24);

    @Setter
    @Value("${qring.email.dns-check.cache-ttl-invalid:1h}")
    private Duration cacheTtlInvalid = Duration.ofHours(1);

    private record Verdict(boolean valid, Instant expiresAt) {}

    /** 도메인 → DNS 판정 캐시. 확정 결과(HAS_MX / NO_MX)만 넣고, 조회 실패는 넣지 않는다. */
    private final Map<String, Verdict> cache = new ConcurrentHashMap<>();

    /**
     * 가입 불가 이메일이면 도메인 오류 코드로 {@link IllegalArgumentException} 을 던진다.
     * 코드: {@code INVALID_EMAIL_DOMAIN}, {@code DISPOSABLE_EMAIL_NOT_ALLOWED}.
     */
    public void validateOrThrow(String email) {
        String domain = normalizeDomain(email);
        if (domain == null || isReserved(domain)) {
            throw new IllegalArgumentException(INVALID_EMAIL_DOMAIN);
        }
        if (disposableEmailService.isDisposable(email)) {
            throw new IllegalArgumentException(DISPOSABLE_EMAIL_NOT_ALLOWED);
        }
        if (dnsCheckEnabled && !hasMailServer(domain)) {
            throw new IllegalArgumentException(INVALID_EMAIL_DOMAIN);
        }
    }

    /** 예외 대신 boolean 이 필요한 곳(check-email)용. 가입 가능한 도메인이면 true. */
    public boolean isAllowed(String email) {
        try {
            validateOrThrow(email);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** 이메일에서 도메인을 뽑아 소문자·punycode 로 정규화. 구조가 잘못됐으면 null. */
    static String normalizeDomain(String email) {
        if (email == null) return null;
        String trimmed = email.trim();
        int at = trimmed.lastIndexOf('@');
        if (at <= 0 || at == trimmed.length() - 1) return null;   // 로컬파트 또는 도메인이 비어 있음

        String domain = trimmed.substring(at + 1).toLowerCase();
        if (domain.endsWith(".")) domain = domain.substring(0, domain.length() - 1);
        try {
            domain = IDN.toASCII(domain);   // 한글 도메인 등 → xn-- 형태
        } catch (IllegalArgumentException e) {
            return null;
        }
        return DOMAIN_PATTERN.matcher(domain).matches() ? domain : null;
    }

    private static boolean isReserved(String domain) {
        String tld = domain.substring(domain.lastIndexOf('.') + 1);
        if (RESERVED_TLDS.contains(tld)) return true;
        for (String reserved : RESERVED_DOMAINS) {
            if (domain.equals(reserved) || domain.endsWith("." + reserved)) return true;
        }
        return false;
    }

    /** 캐시 우선으로 MX 존재 여부 판정. 조회 실패는 거부하되 캐시하지 않아 다음 요청에서 재시도된다. */
    private boolean hasMailServer(String domain) {
        Verdict cached = cache.get(domain);
        if (cached != null && Instant.now().isBefore(cached.expiresAt())) {
            return cached.valid();
        }

        DnsMxResolver.Result result = dnsMxResolver.lookupMx(domain);
        switch (result) {
            case HAS_MX -> {
                cache.put(domain, new Verdict(true, Instant.now().plus(cacheTtlValid)));
                return true;
            }
            case NO_MX -> {
                cache.put(domain, new Verdict(false, Instant.now().plus(cacheTtlInvalid)));
                return false;
            }
            default -> {
                log.warn("[EMAIL DOMAIN] DNS lookup failed, rejecting signup for domain {}", domain);
                return false;
            }
        }
    }
}
