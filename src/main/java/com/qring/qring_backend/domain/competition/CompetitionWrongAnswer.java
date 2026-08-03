package com.qring.qring_backend.domain.competition;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/** 봇 컴피티션 전용 오답 기록 — 스토리 오답(wrong_answer)과 분리 관리. */
@Entity
@Table(
        name = "competition_wrong_answer",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_quiz",
                columnNames = {"user_id", "quiz_content_id"}
        )
)
@Getter @Setter @NoArgsConstructor
public class CompetitionWrongAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wrong_answer_id")
    private Long wrongAnswerId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_content_id", nullable = false)
    private CompetitionQuizContent quizContent;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "lang_code", length = 5, nullable = false)
    private String langCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}