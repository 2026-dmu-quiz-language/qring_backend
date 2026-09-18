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

    /** 비밀번호 규칙 (가입·재설정 공통): 8자 이상, 소문자·숫자 포함, 대문자 또는 특수문자 포함. */
    public static final String PASSWORD_PATTERN =
        "^(?=.*[a-z])(?=.*\\d)(?=.*[A-Z]|.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$";
    public static final String PASSWORD_MESSAGE =
        "Password must contain at least one lowercase, one digit, and one uppercase or special character";

    /** 로컬 회원가입 요청. 학습 설정(language/levelCode)은 선택. */
    @Data
    public static class SignUp {
        @Email @NotBlank
        @Size(max = 100)
        private String email;

        @NotBlank
        @Size(min = 8, max = 100)
        @Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_MESSAGE)
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

    /** 학습 설정(언어/레벨/닉네임) 업데이트 요청. */
    @Data
    public static class UpdatePreferences {
        @NotBlank
        @Pattern(regexp = "^(JA|EN|ZH)$", message = "language must be one of JA/EN/ZH")
        private String language;

        @NotNull
        @Min(1) @Max(3)
        private Integer levelCode;

        // 선택: 소셜 가입 온보딩에서 닉네임 확정 (없으면 기존 닉네임 유지)
        @Size(min = 2, max = 16)
        @Pattern(
            regexp = "^[a-zA-Z0-9가-힣_]+$",
            message = "Nickname can only contain letters, numbers, Korean characters, and underscores"
        )
        private String nickname;
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

    /* ---------- 비밀번호 찾기 (PASSWORD_RESET_DESIGN.md) ---------- */

    /** 1단계: 재설정 코드 발송 요청. */
    @Data
    public static class ForgotPassword {
        @Email @NotBlank
        private String email;
    }

    /** 2단계: 재설정 코드 검증 요청. 성공 시 재설정 토큰을 받는다. */
    @Data
    public static class VerifyResetCode {
        @Email @NotBlank
        private String email;

        @NotBlank
        @Size(min = 6, max = 6)
        private String code;
    }

    /** 3단계: 재설정 토큰으로 새 비밀번호 설정. 규칙은 가입과 같다. */
    @Data
    public static class ResetPassword {
        @NotBlank
        private String resetToken;

        @NotBlank
        @Size(min = 8, max = 100)
        @Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_MESSAGE)
        private String newPassword;
    }
}