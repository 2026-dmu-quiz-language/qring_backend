package com.qring.qring_backend.dto.content;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ChatResponseDto {

    private List<ScriptDto> scripts;
    private List<QuizDto> quizzes;

    @Getter
    @AllArgsConstructor
    public static class ScriptDto {
        private Long scriptId;
        private String characterName;
        private String scriptContent;
        private Boolean hasOptions;
    }

    @Getter
    @AllArgsConstructor
    public static class QuizDto {
        private Long quizId;
        private Long scriptId;
        private String quizType;
        private String question;
        private String options;
        private String correctAnswer;
        private String explanation;
        private String hint;
    }
}
