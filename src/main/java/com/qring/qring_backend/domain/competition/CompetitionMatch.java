package com.qring.qring_backend.domain.competition;

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

/** 봇 컴피티션 매치 세션. entry_cost/reward_point는 매치 시점 스냅샷으로 저장. */
@Entity
@Table(name = "competition_match")
@Getter @Setter @NoArgsConstructor
public class CompetitionMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Long matchId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private MatchStatus status;

    @Column(name = "entry_cost", nullable = false)
    private Integer entryCost;

    @Column(name = "reward_point")
    private Integer rewardPoint;

    @Column(name = "correct_count", nullable = false)
    private Integer correctCount = 0;

    @Column(name = "total_count", nullable = false)
    private Integer totalCount;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "paused_at")
    private LocalDateTime pausedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public enum MatchStatus {
        IN_PROGRESS, PAUSED, COMPLETED, ABANDONED
    }
}