package com.qring.qring_backend.domain.quiz;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** {@link ScoreTable} 복합키 — (난이도, 시도 횟수, 힌트 사용 여부). */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ScoreTableId implements Serializable {
    private int difficulty;
    private int attempt;
    private boolean hintUsed;
}