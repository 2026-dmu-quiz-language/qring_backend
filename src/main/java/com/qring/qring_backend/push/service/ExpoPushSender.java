package com.qring.qring_backend.push.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * Expo Push Notification API 발송기.
 * (qring.push.enabled=true 이고 qring.push.provider!=fcm 일 때 기본 활성화)
 *
 * 프론트엔드가 발급받은 ExponentPushToken[...] 을 받아 Expo 서버(https://exp.host/--/api/v2/push/send)로 전송합니다.
 * Expo 권장에 따라 100건 단위로 청크 분할하여 전송하며,
 * 응답에서 DeviceNotRegistered 오류인 토큰은 invalidTokens 로 수집하여 PushTokenService 에서 자동 정리하도록 합니다.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "qring.push", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "qring.push", name = "provider", havingValue = "expo", matchIfMissing = true)
public class ExpoPushSender implements PushSender {

    static final int CHUNK_SIZE = 100;
    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    private final RestClient restClient;

    public ExpoPushSender() {
        this(RestClient.builder().build());
    }

    public ExpoPushSender(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public PushSendResult send(List<String> tokens, PushMessage message) {
        if (tokens == null || tokens.isEmpty()) {
            return PushSendResult.empty();
        }

        int success = 0;
        int failure = 0;
        List<String> invalid = new ArrayList<>();

        for (int from = 0; from < tokens.size(); from += CHUNK_SIZE) {
            List<String> chunk = tokens.subList(from, Math.min(from + CHUNK_SIZE, tokens.size()));
            List<ExpoPushMessageRequest> requests = chunk.stream()
                    .map(token -> new ExpoPushMessageRequest(
                            token,
                            message.title(),
                            message.body(),
                            message.data(),
                            "default",
                            "high",
                            "default"))
                    .toList();

            try {
                ExpoPushApiResponse response = restClient.post()
                        .uri(EXPO_PUSH_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .body(requests)
                        .retrieve()
                        .body(ExpoPushApiResponse.class);

                if (response != null && response.data() != null) {
                    List<ExpoPushTicket> tickets = response.data();
                    for (int i = 0; i < tickets.size(); i++) {
                        ExpoPushTicket ticket = tickets.get(i);
                        if ("ok".equalsIgnoreCase(ticket.status())) {
                            success++;
                        } else {
                            failure++;
                            if (ticket.details() != null && "DeviceNotRegistered".equals(ticket.details().get("error"))) {
                                if (i < chunk.size()) {
                                    invalid.add(chunk.get(i));
                                }
                            } else {
                                log.warn("[PUSH-EXPO] 발송 실패 message={} details={}", ticket.message(), ticket.details());
                            }
                        }
                    }
                } else {
                    log.warn("[PUSH-EXPO] 빈 응답 수신 chunk.size={}", chunk.size());
                    failure += chunk.size();
                }
            } catch (Exception e) {
                log.error("[PUSH-EXPO] 발송 중 예외 발생 ({}건): {}", chunk.size(), e.getMessage());
                failure += chunk.size();
            }
        }

        return new PushSendResult(success, failure, invalid);
    }

    public record ExpoPushMessageRequest(
            String to,
            String title,
            String body,
            Map<String, String> data,
            String sound,
            String priority,
            String channelId
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ExpoPushApiResponse(
            List<ExpoPushTicket> data,
            List<Map<String, Object>> errors
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ExpoPushTicket(
            String status,
            String id,
            String message,
            Map<String, Object> details
    ) {}
}
