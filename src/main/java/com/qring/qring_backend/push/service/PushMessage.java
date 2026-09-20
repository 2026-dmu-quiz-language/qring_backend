package com.qring.qring_backend.push.service;

import java.util.Map;

/** 발송할 푸시 한 건. data 는 앱이 알림을 탭했을 때 이동할 화면 등을 정하는 문자열 맵 (FCM 제약: 값은 문자열만). */
public record PushMessage(String title, String body, Map<String, String> data) {
}
