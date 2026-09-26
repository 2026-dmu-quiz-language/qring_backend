package com.qring.qring_backend.push.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Firebase Cloud Messaging 발송기 (qring.push.enabled=true 일 때만 빈 등록, FirebaseConfig 가 FirebaseMessaging 을 만든다).
 *
 * 토큰 500개 단위(FCM multicast 상한)로 잘라 보내고, 응답에서 UNREGISTERED / INVALID_ARGUMENT 인 토큰은
 * invalidTokens 로 돌려준다 — 앱을 지웠거나 토큰이 바뀐 기기라 다시 보내도 영원히 실패하는 것들이다.
 * 그 외 오류(쿼터·네트워크·서버 오류)는 실패 개수로만 세고 토큰은 유지한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "qring.push", name = "provider", havingValue = "fcm")
public class FcmPushSender implements PushSender {

    static final int MULTICAST_LIMIT = 500;

    private final FirebaseMessaging firebaseMessaging;

    @Override
    public PushSendResult send(List<String> tokens, PushMessage message) {
        if (tokens == null || tokens.isEmpty()) {
            return PushSendResult.empty();
        }
        int success = 0;
        int failure = 0;
        List<String> invalid = new ArrayList<>();

        for (int from = 0; from < tokens.size(); from += MULTICAST_LIMIT) {
            List<String> chunk = tokens.subList(from, Math.min(from + MULTICAST_LIMIT, tokens.size()));
            try {
                BatchResponse batch = firebaseMessaging.sendEachForMulticast(build(chunk, message));
                success += batch.getSuccessCount();
                failure += batch.getFailureCount();
                List<SendResponse> responses = batch.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    SendResponse r = responses.get(i);
                    if (r.isSuccessful()) continue;
                    FirebaseMessagingException e = r.getException();
                    MessagingErrorCode code = e == null ? null : e.getMessagingErrorCode();
                    if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                        invalid.add(chunk.get(i));
                    } else {
                        log.warn("[PUSH] 발송 실패 code={} msg={}", code, e == null ? null : e.getMessage());
                    }
                }
            } catch (FirebaseMessagingException e) {
                // 묶음 전체가 실패 (인증·네트워크). 토큰 문제는 아니므로 지우지 않는다.
                log.error("[PUSH] multicast 호출 실패 ({}개): {}", chunk.size(), e.getMessage());
                failure += chunk.size();
            }
        }
        return new PushSendResult(success, failure, invalid);
    }

    private static MulticastMessage build(List<String> tokens, PushMessage message) {
        MulticastMessage.Builder b = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(message.title())
                        .setBody(message.body())
                        .build())
                // 학습 리마인더는 도착 시각이 의미 있으므로 HIGH (도즈 모드에서도 바로 표시)
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(AndroidNotification.builder().setSound("default").build())
                        .build())
                .setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder().setSound("default").build())
                        .build());
        if (message.data() != null) {
            b.putAllData(message.data());
        }
        return b.build();
    }
}
