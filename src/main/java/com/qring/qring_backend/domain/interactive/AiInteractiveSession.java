package com.qring.qring_backend.domain.interactive;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** AI 인터렉티브 스토리 생성 대화 세션. 세션 끊김 대비를 위해 메시지는 즉시 저장. */
@Entity
@Table(name = "ai_interactive_session")
@Getter @Setter @NoArgsConstructor
public class AiInteractiveSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status;

    @Column(name = "story_title")
    private String storyTitle;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_activity_at", nullable = false)
    private LocalDateTime lastActivityAt;

    public enum Status {
        IN_PROGRESS, COMPLETED, ABANDONED
    }
}