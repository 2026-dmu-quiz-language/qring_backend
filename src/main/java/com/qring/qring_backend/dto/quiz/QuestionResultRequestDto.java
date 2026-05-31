package com.qring.qring_backend.dto.quiz;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class QuestionResultRequestDto {

    private List<QuizResultDto> results;

    @Getter
    @NoArgsConstructor
    public static class QuizResultDto {
        private Long quizId;
        private int attemptCount;
        private boolean correct;
        private String lastAnswer;
        private boolean hintUsed;
    }
}