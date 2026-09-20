package com.qring.qring_backend.domain.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 모든 포인트 변동 소스를 통합 추적하는 이력 테이블. */
@Entity
@Table(name = "user_asset_history")
@Getter @Setter @NoArgsConstructor
public class UserAssetHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** +면 적립, -면 차감 */
    @Column(name = "change_amount", nullable = false)
    private Integer changeAmount;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 30, nullable = false)
    private SourceType sourceType;

    /** source_type에 따라 다른 대상(match_id, content_id 등)을 가리킴. 숫자 id 가 없는 소스는 null. */
    @Column(name = "reference_id")
    private Long referenceId;

    /** 숫자 id 가 아닌 참조 (인터랙티브 스토리 session_id 등). reference_id 와 둘 중 하나만 쓴다. */
    @Column(name = "reference_key", length = 64)
    private String referenceKey;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 포인트 변동 사유. 컬럼 길이 30 — 새 값은 30자 이내로.
     * reference 규칙: COMPETITION_* → match_id, STORY_* / CONTENT_UNLOCK / INCORRECT_RETRY → content_id,
     * STREAK_REWARD → 달성 연속일(15/30/…), INTERACTIVE_STORY_* → reference_key = session_id, SIGNUP_BONUS → 없음.
     */
    public enum SourceType {
        SIGNUP_BONUS,               // 가입 완료 초기 지급 (+50)
        COMPETITION_ENTRY,          // 봇 컴피티션 입장 (-)
        COMPETITION_REWARD,         // 봇 컴피티션 승리 보상 (+)
        STORY_QUIZ_CORRECT,         // (미사용 — 문항 단위 지급은 하지 않음)
        STORY_QUIZ_WRONG,           // (미사용)
        STORY_LEARNING,             // 스토리 퀴즈 최초 완료 학습 포인트 (+, 최대 100)
        STORY_COMPLETE_BONUS,       // 언어별 첫 스토리 완료 보너스 (+30)
        INCORRECT_RETRY,            // 오답 노트 재풀이 (+, 요청 단위 합산)
        STREAK_REWARD,              // 15일 연속 학습 보상 (+30, 대시보드 조회 시점)
        INTERACTIVE_STORY_CREATE,   // 인터랙티브 스토리 시작 (-)
        INTERACTIVE_STORY_EXTEND,   // 인터랙티브 스토리 이어하기 (-)
        INTERACTIVE_STORY_ARCHIVE,  // 인터랙티브 스토리 보관 (-)
        INTERACTIVE_STORY_REFUND,   // 인터랙티브 스토리 생성 실패 환불 (+)
        CONTENT_UNLOCK              // 콘텐츠 해금 (-)
    }
}