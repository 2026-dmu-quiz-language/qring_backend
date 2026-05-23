package com.qring.qring_backend.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 이메일 인증 코드 생성·발송·검증. 코드는 메모리 맵에 (이메일 → 코드+만료시각) 형태로 보관. */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@qring.local}")
    private String fromAddress;

    @Value("${qring.email.dev-mode:false}")
    private boolean devMode;

    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    private static final SecureRandom RANDOM = new SecureRandom();

    private record Entry(String code, Instant expiresAt) {}

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    /** 새 코드 생성 후 저장소에 기록하고 메일 발송 (devMode면 로그 출력으로 대체). */
    public void sendVerificationCode(String email) {
        String code = generateCode();
        store.put(email, new Entry(code, Instant.now().plus(CODE_TTL)));

        if (devMode) {
            log.info("[DEV MODE] Verification code for {}: {}", email, code);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(email);
            message.setSubject("[Qring] 이메일 인증 코드");
            message.setText(
                "Qring 가입을 환영합니다!\n\n" +
                "인증 코드: " + code + "\n\n" +
                "이 코드는 10분간 유효합니다.\n" +
                "본인이 요청하지 않았다면 이 메일을 무시해 주세요."
            );
            mailSender.send(message);
            log.info("Verification code sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", email, e.getMessage());
            throw new IllegalArgumentException("EMAIL_SEND_FAILED");
        }
    }

    public enum VerifyResult { OK, NOT_FOUND, EXPIRED, MISMATCH }

    /** 입력 코드를 저장소 값과 대조. 성공/만료 시 항목 제거 (1회용). */
    public VerifyResult verifyCode(String email, String code) {
        Entry entry = store.get(email);
        if (entry == null) {
            log.warn("[VERIFY] no code in store for {}", email);
            return VerifyResult.NOT_FOUND;
        }
        if (Instant.now().isAfter(entry.expiresAt())) {
            store.remove(email);
            log.warn("[VERIFY] code expired for {}", email);
            return VerifyResult.EXPIRED;
        }
        if (!entry.code().equals(code)) {
            log.warn("[VERIFY] code mismatch for {}. expected={}, got={}",
                email, entry.code(), code);
            return VerifyResult.MISMATCH;
        }
        store.remove(email);
        return VerifyResult.OK;
    }

    /** 100000~999999 범위의 6자리 숫자 코드. */
    private String generateCode() {
        return String.valueOf(RANDOM.nextInt(900000) + 100000);
    }
}
