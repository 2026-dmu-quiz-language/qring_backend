package com.qring.qring_backend.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class IncorrectResponseDto {

    private List<WrongAnswerSummary> wrongAnswers;

    @Getter
    @AllArgsConstructor
    public static class WrongAnswerSummary {
        private Long contentId;
        private String storyName;
    }
}