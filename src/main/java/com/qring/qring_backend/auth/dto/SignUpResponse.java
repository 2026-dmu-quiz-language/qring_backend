package com.qring.qring_backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 회원가입 1단계(/signup) 응답 — 안내 메시지와 인증 메일 발송 성공 여부. */
@Data
@AllArgsConstructor
public class SignUpResponse {
    private String message;
    private boolean emailSent;
}
