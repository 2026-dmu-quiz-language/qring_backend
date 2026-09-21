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
        private String hint;              // COMPETITION 인 경우 항상 null (해당 필드 없음)
        private String correctAnswer;
        private String quizType;
        private String sourceType;        // STORY / COMPETITION
        private String korean;            // word_arrange 전용 (한글 문장), STORY 는 항상 null
        private String tiles;             // word_arrange 전용 (JSON 배열), STORY 는 항상 null
        private String answerTiles;       // word_arrange 전용 (JSON 배열), STORY 는 항상 null
        private String distractorTiles;   // word_arrange 전용 (JSON 배열), STORY 는 항상 null
    }
}