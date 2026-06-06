package com.qring.qring_backend.auth.dto;

import com.qring.qring_backend.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/** 외부 노출용 사용자 정보 DTO (비밀번호 등 민감 필드 제외). */
@Data
@AllArgsConstructor
public class UserDto {
    private Long userId;
    private String email;
    private String nickname;
    private boolean emailVerified;
    private String authProvider;
    private LocalDateTime createAt;
    private String language;
    private Integer levelCode;

    /** User 엔티티 → UserDto 변환. */
    public static UserDto from(User user) {
        return new UserDto(
            user.getUserId(),
            user.getEmail(),
            user.getNickname(),
            Boolean.TRUE.equals(user.getEmailVerified()),
            user.getAuthProvider(),
            user.getCreateAt(),
            user.getLanguage(),
            user.getLevelCode()
        );
    }
}