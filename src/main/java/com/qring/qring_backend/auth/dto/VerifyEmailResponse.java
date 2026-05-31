package com.qring.qring_backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 이메일 인증(/verify-email) 응답 — 토큰 페어 + 회원가입 성공 여부. 사용자 정보는 /dash·/me에서 별도 조회. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyEmailResponse {
    private String accessToken;
    private String refreshToken;
    private boolean success;
}
