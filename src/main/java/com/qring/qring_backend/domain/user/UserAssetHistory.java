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

    /** source_type에 따라 다른 대상(match_id, quiz_id 등)을 가리킴 */
    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum SourceType {
        COMPETITION_ENTRY,
        COMPETITION_REWARD,
        STORY_QUIZ_CORRECT,
        STORY_QUIZ_WRONG,
        STORY_COMPLETE_BONUS,
        INTERACTIVE_STORY_CREATE
    }
}