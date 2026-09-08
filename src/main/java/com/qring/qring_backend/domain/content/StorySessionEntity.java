package com.qring.qring_backend.domain.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 인터랙티브 스토리 세션의 DB 영속 레코드.
 *
 * ==================================================================================
 * 2026-09-02: DB 담당 팀원이 확정·생성한 실제 테이블과 연결됨.
 * 이 테이블은 자동 스키마 생성(ddl-auto)에서 제외되어 있으므로(StorySchemaFilterProvider),
 * 스키마 변경은 DB 담당자가 실제 테이블에 먼저 반영한 뒤 이 파일을 맞추는 순서로 진행할 것.
 *   - 테이블명 변경 시: 아래 TABLE_NAME 상수 (필터도 이 상수를 따라감)
 *   - 컬럼명 변경 시:  각 필드의 @Column(name = "...")
 * ==================================================================================
 *
 * 수명 주기:
 *   세션 시작 시 status=IN_PROGRESS 로 생성 → 매 턴 갱신
 *   → 완결 후 사용자가 보관을 결제하면 status=ARCHIVED (영구 보관)
 *   → 결제하지 않으면 TTL 경과 시 행 삭제
 */
@Entity
@Table(name = StorySessionEntity.TABLE_NAME,
       indexes = @Index(name = "idx_user_status", columnList = "user_id, status"))
@Getter
@Setter
@NoArgsConstructor
public class StorySessionEntity {

    /** DB 담당자가 확정한 테이블명. */
    public static final String TABLE_NAME = "story_session";

    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_ARCHIVED = "ARCHIVED";

    @Id
    @Column(name = "session_id", length = 50)
    private String sessionId;

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

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "quiz_count", nullable = false)
    private Integer quizCount;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted;

    /** OpenAI 프롬프트용 대화 기록 (최근 40개, 세션 복원에 사용): [{"role","content"}] */
    @Column(name = "chat_history", columnDefinition = "JSON", nullable = false)
    private String chatHistory;

    /**
     * 열람용 통합 타임라인 — 대화·퀴즈·채점이 실제 순서대로 섞여 있으며 잘리지 않는다.
     * [{"type":"message"|"quiz"|"quiz_result", ...}] (다시 읽기 화면은 이 컬럼만 보면 된다)
     */
    @Column(name = "timeline", columnDefinition = "JSON", nullable = false)
    private String timeline;

    /** 세션 복원용 진행 상태: {"turnsSinceLastQuiz":n,"pendingQuiz":{...}|null,"testedQuizSubjects":[...],"usedQuizTypes":[...]} */
    @Column(name = "runtime_state", columnDefinition = "JSON", nullable = false)
    private String runtimeState;

    /**
     * AI 응답 대기 중인 유저 메시지 임시 저장: {"content":"...","sent_at":"..."}.
     * OpenAI 호출 직전에 기록하고 응답을 정상 처리하면 NULL 로 초기화한다.
     * NULL 이 아닌 채로 세션이 복원되면 "AI 응답을 받지 못하고 끊겼다"는 뜻이며 복구 로직이 감지해 정리한다.
     */
    @Column(name = "pending_user_message", columnDefinition = "JSON")
    private String pendingUserMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;
}
