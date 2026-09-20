package com.qring.qring_backend.push.service;

import java.util.List;

/**
 * 푸시 발송 추상화. 구현은 두 가지 — FcmPushSender(실제 FCM), LogPushSender(qring.push.enabled=false 일 때 로그만).
 * 서비스 계층은 이 인터페이스만 보고, 발송 채널 교체나 테스트 시 mock 으로 갈아 끼운다.
 */
public interface PushSender {

    /** 토큰 목록에 같은 메시지를 보낸다. 토큰이 비어 있으면 아무것도 하지 않고 empty 결과. */
    PushSendResult send(List<String> tokens, PushMessage message);
}
