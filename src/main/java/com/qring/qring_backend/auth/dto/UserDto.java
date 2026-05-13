package com.qring.qring_backend.auth.dto;

import com.qring.qring_backend.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserDto {
    private Long userId;
    private String email;
    private String nickname;
    private String storyNickname;
    private boolean emailVerified;
    private String authProvider;
    private LocalDateTime createdAt;
    private String language;
    private Integer levelCode;

    public static UserDto from(User user) {
        return new UserDto(
            user.getUserId(),
            user.getEmail(),
            user.getNickname(),
            user.getStoryNickname(),
            Boolean.TRUE.equals(user.getEmailVerified()),
            user.getAuthProvider(),
            user.getCreateAt(),
            user.getLanguage(),
            user.getLevelCode()
        );
    }
}
