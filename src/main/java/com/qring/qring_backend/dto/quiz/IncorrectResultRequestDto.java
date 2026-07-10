package com.qring.qring_backend.dto.quiz;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class IncorrectResultRequestDto {

    private Long contentId;
    private List<QuizResultDto> results;

    @Getter
    @NoArgsConstructor
    public static class QuizResultDto {
        private Long quizContentId;
        private boolean correct;
    }
}