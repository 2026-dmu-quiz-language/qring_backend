package com.qring.qring_backend.dto.quiz;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuestionResultRequestDto {

    private Long quizId;
    private int attemptCount;
    private boolean isCorrect;
    private String lastAnswer;
    private boolean hintUsed;
}
