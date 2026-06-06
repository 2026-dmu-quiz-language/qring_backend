package com.qring.qring_backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 인증 관련 요청 DTO 모음 — 회원가입·로그인·이메일 인증·소셜 로그인·토큰 갱신 등. */
public class AuthRequest {

    /** 로컬 회원가입 요청. 학습 설정(language/levelCode)은 선택. */
    @Data
    public static class SignUp {
        @Email @NotBlank
        @Size(max = 100)
        private String email;

        @NotBlank
        @Size(min = 8, max = 100)
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[A-Z]|.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$",
            message = "Password must contain at least one lowercase, one digit, and one uppercase or special character"
        )
        private String password;

        @NotBlank
        @Size(min = 2, max = 16)
        @Pattern(
            regexp = "^[a-zA-Z0-9가-힣_]+$",
            message = "Nickname can only contain letters, numbers, Korean characters, and underscores"
        )
        private String nickname;

        // 선택: 회원가입 시 학습 설정을 함께 받음 (없으면 이후 OnboardingScreen에서 설정)
        @Pattern(regexp = "^(JA|EN|ZH)$", message = "language must be one of JA/EN/ZH")
        private String language;

        @Min(1) @Max(3)
        private Integer levelCode;
    }

    /** 학습 설정(언어/레벨) 업데이트 요청. */
    @Data
    public static class UpdatePreferences {
        @NotBlank
        @Pattern(regexp = "^(JA|EN|ZH)$", message = "language must be one of JA/EN/ZH")
        private String language;

        @NotNull
        @Min(1) @Max(3)
        private Integer levelCode;
    }

    /** 이메일 인증 코드 검증 요청. */
    @Data
    public static class VerifyEmail {
        @Email @NotBlank
        private String email;

        @NotBlank
        @Size(min = 6, max = 6)
        private String code;
    }

    /** 인증 코드 재발송 요청. */
    @Data
    public static class ResendCode {
        @Email @NotBlank
        private String email;
    }

    /** 로컬 로그인 요청. */
    @Data
    public static class Login {
        @Email @NotBlank
        private String email;

        @NotBlank
        private String password;
    }

    /** 소셜 로그인 요청 — Google은 ID 토큰, Kakao/LINE은 authorization code를 token에 담아 전달. */
    @Data
    public static class SocialLogin {
        @NotBlank
        private String token;

        private String redirectUri;
    }

    /** 리프레시 토큰 갱신 요청. */
    @Data
    public static class RefreshToken {
        @NotBlank
        private String refreshToken;
    }
}