package com.qring.qring_backend.domain.quiz;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ScoreTableId implements Serializable {
    private int difficulty;
    private int attempt;
    private boolean hintUsed;
}