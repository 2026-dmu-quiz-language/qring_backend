package com.qring.qring_backend.push.service;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * qring.push.enabled=false (기본값) 일 때 쓰는 발송기. 실제로 보내지 않고 로그에 남기며 전부 성공으로 친다.
 * 로컬 개발·CI 에서 Firebase 키 없이 기동하고, 스케줄러 로직을 로그로 확인하는 용도.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "qring.push", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LogPushSender implements PushSender {

    @Override
    public PushSendResult send(List<String> tokens, PushMessage message) {
        if (tokens == null || tokens.isEmpty()) {
            return PushSendResult.empty();
        }
        log.info("[PUSH DEV MODE] {}개 토큰으로 발송 생략 — title='{}' body='{}' data={}",
                tokens.size(), message.title(), message.body(), message.data());
        return new PushSendResult(tokens.size(), 0, List.of());
    }
}
