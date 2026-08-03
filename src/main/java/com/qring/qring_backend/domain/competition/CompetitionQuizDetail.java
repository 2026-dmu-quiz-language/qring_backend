package com.qring.qring_backend.domain.competition;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 봇 컴피티션 문제 — 언어 무관 필드(레벨, 원본 순번, 문제 유형). */
@Entity
@Table(
        name = "competition_quiz_detail",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_level_origin",
                columnNames = {"level", "origin_id"}
        )
)
@Getter @Setter @NoArgsConstructor
public class CompetitionQuizDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id")
    private Long quizId;

    @Column(name = "level", nullable = false)
    private Integer level;

    /** json 원본의 id (레벨 내 순번) — 언어 간 문제 매칭 키 */
    @Column(name = "origin_id", nullable = false)
    private Integer originId;

    @Column(name = "quiz_type", length = 30, nullable = false)
    private String quizType;
}