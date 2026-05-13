package com.qring.qring_backend.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

public class AuthRequest {

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

        @Size(max = 50)
        private String storyNickname;

        // 선택: 회원가입 시 학습 설정을 함께 받음 (없으면 이후 OnboardingScreen에서 설정)
        @Pattern(regexp = "^(JA|EN|ZH)$", message = "language must be one of JA/EN/ZH")
        private String language;

        @Min(1) @Max(3)
        private Integer levelCode;
    }

    @Data
    public static class UpdatePreferences {
        @NotBlank
        @Pattern(regexp = "^(JA|EN|ZH)$", message = "language must be one of JA/EN/ZH")
        private String language;

        @NotNull
        @Min(1) @Max(3)
        private Integer levelCode;
    }

    @Data
    public static class VerifyEmail {
        @Email @NotBlank
        private String email;

        @NotBlank
        @Size(min = 6, max = 6)
        private String code;
    }

    @Data
    public static class ResendCode {
        @Email @NotBlank
        private String email;
    }

    @Data
    public static class Login {
        @Email @NotBlank
        private String email;

        @NotBlank
        private String password;
    }

    @Data
    public static class SocialLogin {
        @NotBlank
        private String token;

        private String redirectUri;
    }

    @Data
    public static class RefreshToken {
        @NotBlank
        private String refreshToken;
    }
}
