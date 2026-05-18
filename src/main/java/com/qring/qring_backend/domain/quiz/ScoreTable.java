package com.qring.qring_backend.domain.quiz;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "score_table")
@Getter
@NoArgsConstructor
@IdClass(ScoreTableId.class) // 복합키 매핑을 위한 클래스 지정
public class ScoreTable {

    @Id
    private int difficulty;

    @Id
    private int attempt;

    @Id
    @Column(name = "hint_used", columnDefinition = "BIT(1)")
    private boolean hintUsed;

    @Column(nullable = false)
    private int score;
}

@Getter
@NoArgsConstructor
class ScoreTableId implements Serializable {
    private int difficulty;
    private int attempt;
    private boolean hintUsed;

    public ScoreTableId(int difficulty, int attempt, boolean hintUsed) {
        this.difficulty = difficulty;
        this.attempt = attempt;
        this.hintUsed = hintUsed;
    }
    // Equals & HashCode 생략 (Lombok @EqualsAndHashCode 추가 가능)
}
