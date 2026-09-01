package com.qring.qring_backend.domain.interactive;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 대화 한 턴(유저 프롬프트 또는 AI 응답). 유저가 메시지를 보내는 즉시,
 * AI 응답이 오는 즉시 각각 저장해서 앱 종료/네트워크 끊김에도 서버엔 남아있게 함.
 */
@Entity
@Table(
        name = "ai_interactive_message",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_session_client_msg",
                columnNames = {"session_id", "client_message_id"}
        )
)
@Getter @Setter @NoArgsConstructor
public class AiInteractiveMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private AiInteractiveSession session;

    @Column(name = "turn_no", nullable = false)
    private Integer turnNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 10, nullable = false)
    private Role role;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /** 프론트가 생성한 고유값 - 재전송돼도 (session_id, client_message_id) 유니크로 중복 저장 방지 */
    @Column(name = "client_message_id", length = 100, nullable = false)
    private String clientMessageId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum Role {
        USER, AI
    }
}