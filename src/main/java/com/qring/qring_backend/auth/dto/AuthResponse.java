package com.qring.qring_backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 소셜 로그인(OAuth) 응답 — 토큰 페어 + 신규 가입 여부. 사용자 정보는 /dash·/me에서 별도 조회. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    /** 이 OAuth 호출로 새로 가입된 사용자이면 true. OnboardingScreen 분기에 사용. */
    private boolean isNewUser;
}
