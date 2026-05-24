package com.qring.qring_backend.domain.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/** 사용자 엔티티 — 로컬/소셜 가입 공용. authProvider=LOCAL이면 password 사용. */
@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(unique = true, length = 50)
    private String nickname;

    @Column(name = "story_nickname", length = 50)
    private String storyNickname;

    @Column(name = "create_at")
    @Builder.Default
    private LocalDateTime createAt = LocalDateTime.now();

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "auth_provider", nullable = false, length = 20)
    @Builder.Default
    private String authProvider = "LOCAL";

    @Column(name = "social_id", length = 100)
    private String socialId;

    @Column(length = 10)
    private String language;

    @Column(name = "level_code")
    private Integer levelCode;
}
