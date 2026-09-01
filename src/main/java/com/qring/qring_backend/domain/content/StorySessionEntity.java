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
 * !! 테이블명과 모든 컬럼명은 백엔드에서 임시로 붙인 "제안값"입니다 !!
 * 실제 이름은 DB 담당 팀원이 테이블을 설계하면서 자유롭게 확정/변경하면 됩니다.
 *
 * 이름 변경 방법 (이 파일만 고치면 됩니다):
 *   - 테이블명: 아래 TABLE_NAME 상수 하나만 변경
 *     (자동 생성 제외 필터 StorySchemaFilterProvider 도 이 상수를 참조하므로 함께 따라갑니다)
 *   - 컬럼명:  각 필드의 @Column(name = "...") 값만 변경
 *     (Repository 는 JPQL/파생 쿼리라 자바 필드명 기준으로 동작 — 다른 코드는 영향 없음)
 *   - 인덱스명/구성: @Table 의 @Index 값 변경
 * ==================================================================================
 *
 * 수명 주기:
 *   세션 시작 시 status=IN_PROGRESS 로 생성 → 매 턴 갱신
 *   → 완결 후 사용자가 보관을 결제하면 status=ARCHIVED (영구 보관)
 *   → 결제하지 않으면 TTL 경과 시 행 삭제
 */
@Entity
@Table(name = StorySessionEntity.TABLE_NAME,
       indexes = @Index(name = "idx_story_session_user_status", columnList = "user_id, status"))
@Getter
@Setter
@NoArgsConstructor
public class StorySessionEntity {

    /** [제안값] 테이블명 — DB 담당자가 확정하면 이 상수만 바꾸면 된다. */
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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;
}
