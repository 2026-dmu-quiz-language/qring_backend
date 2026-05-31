package com.qring.qring_backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 로컬 로그인(/login) 응답 — 토큰 페어만 반환. 사용자 정보는 /dash·/me에서 별도 조회. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
}
