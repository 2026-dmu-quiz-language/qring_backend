package com.qring.qring_backend.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class IncorrectRetryResponseDto {

    private List<IncorrectQuizDto> quizzes;

    @Getter
    @AllArgsConstructor
    public static class IncorrectQuizDto {
        private Long quizContentId;
        private String question;
        private String options;
        private String hint;          // COMPETITION 인 경우 항상 null (해당 필드 없음)
        private String correctAnswer;
        private String quizType;
        private String sourceType;    // STORY / COMPETITION
    }
}