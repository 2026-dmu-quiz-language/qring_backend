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
        private Integer level;            // STORY: quiz_detail.difficulty / 컴피티션 오답노트: 매치 레벨

        /**
         * STORY 원본 문제용 생성자 (8개 인자). IncorrectService.toStoryQuizDto 가 사용한다.
         * word_arrange 전용 필드(korean/tiles/answerTiles/distractorTiles)는 null 로 둔다.
         */
        public IncorrectQuizDto(Long quizContentId, String question, String options, String hint,
                                String correctAnswer, String quizType, String sourceType, Integer level) {
            this(quizContentId, question, options, hint, correctAnswer, quizType, sourceType,
                 null, null, null, null, level);
        }
    }
}