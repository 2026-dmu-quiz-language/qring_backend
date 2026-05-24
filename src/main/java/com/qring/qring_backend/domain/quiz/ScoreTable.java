package com.qring.qring_backend.domain.quiz;

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
@IdClass(ScoreTableId.class) 
public class ScoreTable {

    @Id
    @Column(name = "difficulty")
    private int difficulty;

    @Id
    @Column(name = "attempt")
    private int attempt;

    @Id
    @Column(name = "hint_used")
    private boolean hintUsed;

    @Column(name = "score", nullable = false)
    private int score;
}