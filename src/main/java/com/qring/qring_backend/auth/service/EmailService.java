package com.qring.qring_backend.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 이메일 인증 코드 생성·발송·검증.
 *
 * 코드는 메모리 맵에 (용도:이메일 → 코드+만료시각+발급시각+오답횟수) 형태로 보관한다.
 * 용도(Purpose)를 키에 넣어 가입 인증 코드로 비밀번호 재설정을 하거나 그 반대가 되지 않게 한다.
 *
 * 2026-09-18 추가 (비밀번호 찾기 설계, PASSWORD_RESET_DESIGN.md):
 *   - 같은 이메일·용도로 60초 안에 다시 요청하면 거부 (메일 폭탄 방지)
 *   - 오답 5회면 코드 폐기 (6자리 숫자 무차별 대입 방지). 가입 인증에도 같이 적용 (팀 결정)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@qring.local}")
    private String fromAddress;

    @Value("${qring.email.dev-mode:false}")
    private boolean devMode;

    /** 코드 용도. 저장소 키와 메일 본문이 용도별로 다르다. */
    public enum Purpose { SIGNUP, PASSWORD_RESET }

    public static final Duration CODE_TTL = Duration.ofMinutes(10);
    /** 같은 이메일·용도로 재발송을 허용하기까지의 최소 간격. */
    public static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    /** 이 횟수만큼 틀리면 코드를 폐기한다. */
    public static final int MAX_VERIFY_ATTEMPTS = 5;

    private static final SecureRandom RANDOM = new SecureRandom();

    /** 시각 소스 (테스트에서 바꿔 끼울 수 있게 필드로 둔다). */
    private Clock clock = Clock.systemUTC();

    private static final class Entry {
        final String code;
        final Instant issuedAt;
        final Instant expiresAt;
        int failedAttempts;

        Entry(String code, Instant issuedAt, Instant expiresAt) {
            this.code = code;
            this.issuedAt = issuedAt;
            this.expiresAt = expiresAt;
        }
    }

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    /** 회원 탈퇴 시 해당 이메일로 발급된 코드(가입·비밀번호 재설정)를 모두 폐기한다. */
    public void discardCodes(String email) {
        for (Purpose purpose : Purpose.values()) {
            store.remove(key(email, purpose));
        }
    }

    /* ------------------------------ 가입 인증 (기존 API 유지) ------------------------------ */

    /** 가입 인증 코드 발송. (기존 호출부 호환) */
    public void sendVerificationCode(String email) {
        sendCode(email, Purpose.SIGNUP);
    }

    /** 가입 인증 코드 검증. (기존 호출부 호환) */
    public VerifyResult verifyCode(String email, String code) {
        return verifyCode(email, code, Purpose.SIGNUP);
    }

    /* ------------------------------ 용도별 API ------------------------------ */

    /**
     * 새 코드 생성 후 저장소에 기록하고 메일 발송 (devMode면 로그 출력으로 대체).
     * 60초 안에 같은 이메일·용도로 다시 부르면 CODE_RESEND_COOLDOWN 예외.
     */
    public void sendCode(String email, Purpose purpose) {
        String key = key(email, purpose);
        Instant now = clock.instant();

        Entry existing = store.get(key);
        if (existing != null && now.isBefore(existing.issuedAt.plus(RESEND_COOLDOWN))) {
            long wait = Duration.between(now, existing.issuedAt.plus(RESEND_COOLDOWN)).getSeconds();
            log.info("[EMAIL] resend cooldown for {} ({}): {}s left", email, purpose, wait);
            throw new IllegalArgumentException("CODE_RESEND_COOLDOWN");
        }

        String code = generateCode();
        store.put(key, new Entry(code, now, now.plus(CODE_TTL)));

        if (devMode) {
            log.info("[DEV MODE] {} code for {}: {}", purpose, email, code);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(email);
            message.setSubject(subjectFor(purpose));
            message.setText(bodyFor(purpose, code));
            mailSender.send(message);
            log.info("{} code sent to {}", purpose, email);
        } catch (Exception e) {
            log.error("Failed to send {} email to {}: {}", purpose, email, e.getMessage());
            store.remove(key); // 발송에 실패한 코드는 쿨다운에 걸리지 않도록 지운다
            throw new IllegalArgumentException("EMAIL_SEND_FAILED");
        }
    }

    public enum VerifyResult { OK, NOT_FOUND, EXPIRED, MISMATCH, TOO_MANY_ATTEMPTS }

    /**
     * 입력 코드를 저장소 값과 대조. 성공/만료/횟수 초과 시 항목 제거 (1회용).
     * 틀릴 때마다 횟수를 세고 MAX_VERIFY_ATTEMPTS 에 이르면 코드를 폐기한다.
     */
    public VerifyResult verifyCode(String email, String code, Purpose purpose) {
        String key = key(email, purpose);
        Entry entry = store.get(key);
        if (entry == null) {
            log.warn("[VERIFY] no {} code in store for {}", purpose, email);
            return VerifyResult.NOT_FOUND;
        }
        if (clock.instant().isAfter(entry.expiresAt)) {
            store.remove(key);
            log.warn("[VERIFY] {} code expired for {}", purpose, email);
            return VerifyResult.EXPIRED;
        }
        if (!entry.code.equals(code)) {
            int attempts = ++entry.failedAttempts;
            if (attempts >= MAX_VERIFY_ATTEMPTS) {
                store.remove(key);
                log.warn("[VERIFY] {} code discarded for {} after {} wrong attempts", purpose, email, attempts);
                return VerifyResult.TOO_MANY_ATTEMPTS;
            }
            log.warn("[VERIFY] {} code mismatch for {} ({}/{})", purpose, email, attempts, MAX_VERIFY_ATTEMPTS);
            return VerifyResult.MISMATCH;
        }
        store.remove(key);
        return VerifyResult.OK;
    }

    /** 결과를 도메인 오류 코드로 바꾼다 (OK 면 아무것도 하지 않는다). 컨트롤러 계층에서 400 으로 변환된다. */
    public static void throwIfNotOk(VerifyResult result) {
        switch (result) {
            case NOT_FOUND -> throw new IllegalArgumentException("CODE_NOT_FOUND_OR_EXPIRED");
            case EXPIRED -> throw new IllegalArgumentException("CODE_EXPIRED");
            case MISMATCH -> throw new IllegalArgumentException("CODE_MISMATCH");
            case TOO_MANY_ATTEMPTS -> throw new IllegalArgumentException("TOO_MANY_ATTEMPTS");
            case OK -> { }
        }
    }

    private static String key(String email, Purpose purpose) {
        return purpose.name() + ":" + (email == null ? "" : email.trim().toLowerCase());
    }

    private static String subjectFor(Purpose purpose) {
        return purpose == Purpose.PASSWORD_RESET ? "[Qring] 비밀번호 재설정 코드" : "[Qring] 이메일 인증 코드";
    }

    private static String bodyFor(Purpose purpose, String code) {
        if (purpose == Purpose.PASSWORD_RESET) {
            return "Qring 비밀번호 재설정 요청입니다.\n\n" +
                "인증 코드: " + code + "\n\n" +
                "이 코드는 10분간 유효합니다.\n" +
                "본인이 요청하지 않았다면 이 메일을 무시해 주세요. 비밀번호는 변경되지 않습니다.";
        }
        return "Qring 가입을 환영합니다!\n\n" +
            "인증 코드: " + code + "\n\n" +
            "이 코드는 10분간 유효합니다.\n" +
            "본인이 요청하지 않았다면 이 메일을 무시해 주세요.";
    }

    /** 100000~999999 범위의 6자리 숫자 코드. */
    private String generateCode() {
        return String.valueOf(RANDOM.nextInt(900000) + 100000);
    }

    /* ------------------------------ 테스트 지원 ------------------------------ */

    void setClock(Clock clock) {
        this.clock = clock;
    }

    void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    /** 테스트용: 저장된 코드 조회 (없으면 null). */
    String peekCode(String email, Purpose purpose) {
        Entry entry = store.get(key(email, purpose));
        return entry == null ? null : entry.code;
    }
}
