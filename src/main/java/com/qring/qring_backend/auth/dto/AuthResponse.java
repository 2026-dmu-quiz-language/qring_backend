package com.qring.qring_backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 인증 성공 응답 — JWT 토큰 페어 + 사용자 기본 정보 + 신규 가입 여부. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String email;
    private String nickname;
    private String authProvider;
    private boolean isNewUser;
    private String language;
    private Integer levelCode;
}
