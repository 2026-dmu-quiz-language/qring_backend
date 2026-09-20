package com.qring.qring_backend.push.service;

import java.util.List;

/**
 * 토큰 묶음 발송 결과. invalidTokens 는 FCM 이 "더 이상 유효하지 않다"고 답한 토큰
 * (앱 삭제·토큰 갱신 등) — 호출자가 DB 에서 지워야 한다. 일시 오류(네트워크·쿼터)는 실패로만 세고 토큰은 남긴다.
 */
public record PushSendResult(int successCount, int failureCount, List<String> invalidTokens) {

    public static PushSendResult empty() {
        return new PushSendResult(0, 0, List.of());
    }
}
