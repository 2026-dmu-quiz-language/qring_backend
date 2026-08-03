package com.qring.qring_backend.dto.competition;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 영우 오빠가 주는 봇 컴피티션 문제 json 최상위 구조 그대로 매핑. */
@Getter @Setter @NoArgsConstructor
public class CompetitionQuizImportDto {

    private String quizSet;
    private String language;
    private List<LevelDto> levels;

    @Getter @Setter @NoArgsConstructor
    public static class LevelDto {
        private Integer level;
        private List<QuestionDto> questions;
    }

    @Getter @Setter @NoArgsConstructor
    public static class QuestionDto {
        private Integer id;              // origin_id로 저장됨
        private String type;             // quiz_type
        private String question;
        private String korean;
        private List<String> tiles;
        private List<String> answerTiles;
        private List<String> distractorTiles;
        private List<String> options;
        private String answer;
        private List<String> acceptableAnswers;
    }
}