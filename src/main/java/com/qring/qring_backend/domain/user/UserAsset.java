package com.qring.qring_backend.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 사용자별 자산 — 보유 포인트·누적 경험치·연속 학습일. User와 1:1. */
@Entity
@Table(name = "User_Asset")
@Getter @Setter @NoArgsConstructor
public class UserAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asset_id")
    private Long assetId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "current_points")
    private Integer currentPoints;

    @Column(name = "total_exp")
    private Integer totalExp;

    @Column(name = "streak_days")
    private Integer streakDays;
}