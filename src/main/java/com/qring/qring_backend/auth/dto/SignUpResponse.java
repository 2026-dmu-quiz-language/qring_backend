package com.qring.qring_backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 회원가입 1단계(/signup) 응답 — 가입 처리 상태와 안내 메시지. */
@Data
@AllArgsConstructor
public class SignUpResponse {
    private Long userId;
    private String signupStatus;
    private String message;
}
