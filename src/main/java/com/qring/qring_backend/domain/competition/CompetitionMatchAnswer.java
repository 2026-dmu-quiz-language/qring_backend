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

/** 매치 내 라운드별 유저-봇 답안 기록. 타이밍 값은 저장하지 않고 결과(승/패/무)만 저장. */
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_content_id", nullable = false)
    private CompetitionQuizContent quizContent;

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

    public enum RoundWinner {
        USER, BOT, DRAW
    }
}