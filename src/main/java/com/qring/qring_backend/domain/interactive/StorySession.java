package com.qring.qring_backend.domain.interactive;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * AI 인터렉티브 스토리 대화 세션. 대화 기록을 JSON 컬럼(chat_history/timeline)에 통합 관리.
 * pending_user_message는 AI 응답을 기다리는 동안 앱 종료/네트워크 끊김이 나도
 * 유저가 방금 보낸 메시지가 유실되지 않도록 즉시 저장해두는 임시 슬롯.
 */
@Entity
@Table(
        name = "story_session",
        indexes = @Index(name = "idx_user_status", columnList = "user_id, status")
)
@Getter @Setter @NoArgsConstructor
public class StorySession {

    @Id
    @Column(name = "session_id", length = 50)
    private String sessionId; // 애플리케이션에서 "sess-" + UUID로 생성

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "character_name", length = 100, nullable = false)
    private String characterName;

    @Column(name = "situation_description", columnDefinition = "TEXT", nullable = false)
    private String situationDescription;

    @Column(name = "tone", length = 50, nullable = false)
    private String tone;

    @Column(name = "target_language", length = 30, nullable = false)
    private String targetLanguage;

    @Column(name = "level_code", nullable = false)
    private Integer levelCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status;

    @Column(name = "quiz_count", nullable = false)
    private Integer quizCount;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted;

    /** OpenAI 프롬프트용 대화 기록 (최근 40개, 세션 복원용) - JSON 문자열 */
    @Column(name = "chat_history", columnDefinition = "JSON", nullable = false)
    private String chatHistory;

    /** 열람용 통합 기록 (대화/퀴즈/채점 실제 순서, 안 잘림) - JSON 문자열 */
    @Column(name = "timeline", columnDefinition = "JSON", nullable = false)
    private String timeline;

    /** 세션 복원용 진행 상태 (대기 퀴즈, 페이싱 카운터 등) - JSON 문자열 */
    @Column(name = "runtime_state", columnDefinition = "JSON", nullable = false)
    private String runtimeState;

    /**
     * AI 응답 대기 중인 유저 메시지 임시 저장 (content, clientMessageId, sentAt).
     * AI 응답 오면 null로 초기화. null이 아닌 채로 세션이 재개되면
     * "AI 응답을 못 받은 채 끊겼다"는 뜻이므로 복구 로직에서 사용.
     */
    @Column(name = "pending_user_message", columnDefinition = "JSON")
    private String pendingUserMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 마지막 턴 시각 - TTL 판단 기준

    @Column(name = "archived_at")
    private LocalDateTime archivedAt; // 보관 결제 시각, 미보관이면 null

    public enum Status {
        IN_PROGRESS, ARCHIVED
    }
}