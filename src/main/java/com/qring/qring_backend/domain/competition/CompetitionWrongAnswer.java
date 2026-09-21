package com.qring.qring_backend.domain.competition;

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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 봇 컴피티션 오답 기록.
 * 매치에서 틀린 문제는 원본이 스토리든 컴피티션 전용이든 상관없이 "그 매치의 레벨" 오답으로 묶는다.
 * 원본 테이블 구분은 sourceType 으로만 하고, FK 는 걸지 않는다 (story/competition 두 quiz_content 테이블의
 * PK 값 범위가 겹칠 수 있어 FK 하나로는 구분 불가능하기 때문).
 */
@Entity
@Table(
        name = "competition_wrong_answer",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_quiz_source",
                columnNames = {"user_id", "quiz_content_id", "source_type"}
        )
)
@Getter @Setter @NoArgsConstructor
public class CompetitionWrongAnswer {

    public enum SourceType { STORY, COMPETITION }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wrong_answer_id")
    private Long wrongAnswerId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 원본 문제의 quiz_content_id. sourceType 이 STORY 면 quiz_content(스토리), COMPETITION 이면 competition_quiz_content 의 PK. */
    @Column(name = "quiz_content_id", nullable = false)
    private Long quizContentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 20, nullable = false)
    private SourceType sourceType;

    /** 그 문제를 틀린 매치의 레벨 (오답노트에서 "레벨 N 컴피티션"으로 묶는 기준) */
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