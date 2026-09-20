package com.qring.qring_backend.domain.user;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 푸시 알림 수신용 기기 토큰 (FCM registration token).
 *
 * 한 사용자가 여러 기기를 쓸 수 있으므로 users 컬럼이 아니라 별도 테이블로 둔다.
 * token 은 기기(앱 설치) 단위로 유일하다 — 같은 기기에서 다른 계정으로 로그인하면
 * 새 사용자에게 재배정한다 (PushTokenService.register). 설계: PUSH_NOTIFICATION_DESIGN.md
 */
@Entity
@Table(name = "user_device_token",
       uniqueConstraints = @UniqueConstraint(name = "uk_user_device_token_token", columnNames = "token"),
       indexes = @Index(name = "idx_user_device_token_user", columnList = "user_id"))
@Getter
@Setter
@NoArgsConstructor
public class UserDeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** FCM 토큰. 보통 150~200자지만 여유를 두고 512. */
    @Column(name = "token", nullable = false, length = 512)
    private String token;

    /** ANDROID / IOS / WEB 등 클라이언트가 보내는 값 그대로 (통계·디버깅용, 발송 로직에는 쓰지 않음). */
    @Column(name = "platform", length = 20)
    private String platform;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 마지막으로 앱이 이 토큰을 등록(갱신)한 시각. 오래 갱신되지 않은 토큰 정리 기준으로 쓸 수 있다. */
    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt;
}
