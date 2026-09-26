package com.qring.qring_backend.config;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import lombok.extern.slf4j.Slf4j;

/**
 * Firebase Admin SDK 초기화. qring.push.enabled=true 일 때만 동작한다 (설계: PUSH_NOTIFICATION_DESIGN.md).
 *
 * 서비스 계정 키는 두 방식 중 하나로 준다:
 *   - FIREBASE_CREDENTIALS_PATH : 키 json 파일 경로 (로컬 개발)
 *   - FIREBASE_CREDENTIALS_JSON : 키 json 원문 또는 base64 (컨테이너 환경변수/시크릿)
 * 둘 다 비어 있으면 기동 실패 — 켜 놓고 조용히 발송이 안 되는 상황을 막기 위해서다.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "qring.push", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "qring.push", name = "provider", havingValue = "fcm")
public class FirebaseConfig {

    @Value("${qring.push.firebase.credentials-path:}")
    private String credentialsPath;

    @Value("${qring.push.firebase.credentials-json:}")
    private String credentialsJson;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }
        try (InputStream in = openCredentials()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(in))
                    .build();
            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("[PUSH] FirebaseApp 초기화 완료 (project={})", app.getOptions().getProjectId());
            return app;
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp app) {
        return FirebaseMessaging.getInstance(app);
    }

    private InputStream openCredentials() throws IOException {
        if (credentialsPath != null && !credentialsPath.isBlank()) {
            return new FileInputStream(credentialsPath.trim());
        }
        if (credentialsJson != null && !credentialsJson.isBlank()) {
            String raw = credentialsJson.trim();
            byte[] bytes = raw.startsWith("{")
                    ? raw.getBytes(StandardCharsets.UTF_8)
                    : Base64.getDecoder().decode(raw);
            return new ByteArrayInputStream(bytes);
        }
        throw new IllegalStateException(
                "qring.push.enabled=true 이지만 FIREBASE_CREDENTIALS_PATH / FIREBASE_CREDENTIALS_JSON 이 모두 비어 있습니다.");
    }
}
