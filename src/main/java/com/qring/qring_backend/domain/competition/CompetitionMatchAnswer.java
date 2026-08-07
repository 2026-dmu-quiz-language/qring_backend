package com.qring.qring_backend.domain.competition;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/**
 * 매치 내 라운드별 유저-봇 답안 기록.
 * 문제 출처가 competition_quiz_content 또는 quiz_content(스토리) 둘 중 하나일 수 있어서
 * FK 대신 source_type + source_quiz_content_id 조합으로 구분해서 저장.
 */
@Entity
@Table(
        name = "competition_match_answer",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_match_round",
                columnNames = {"match_id", "round_no"}
        )
)
@Getter @Setter @NoArgsConstructor
public class CompetitionMatchAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_answer_id")
    private Long matchAnswerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private CompetitionMatch match;

    /** STORY면 quiz_content.quiz_content_id, COMPETITION이면 competition_quiz_content.quiz_content_id */
    @Column(name = "source_quiz_content_id", nullable = false)
    private Long sourceQuizContentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 15, nullable = false)
    private SourceType sourceType;

    @Column(name = "round_no", nullable = false)
    private Integer roundNo;

    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "user_is_correct", nullable = false)
    private Boolean userIsCorrect;

    @Column(name = "bot_is_correct", nullable = false)
    private Boolean botIsCorrect;

    @Enumerated(EnumType.STRING)
    @Column(name = "round_winner", length = 10, nullable = false)
    private RoundWinner roundWinner;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum SourceType {
        STORY, COMPETITION
    }

    public enum RoundWinner {
        USER, BOT, DRAW
    }
}