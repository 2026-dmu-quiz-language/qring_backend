package com.qring.qring_backend.dto.competition;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 문제 출처(스토리/컴피티션)와 관계없이 프론트에 통일된 형태로 내려주는 문제 DTO.
 * source 정보는 /bot/result 제출 시 그대로 돌려받아 어느 테이블 문제인지 식별하는 데 사용.
 */
@Getter
@AllArgsConstructor
public class CompetitionQuizItemDto {

    private String sourceType;       // STORY / COMPETITION
    private Long sourceQuizContentId; // quiz_content_id (출처 테이블 기준)
    private String quizType;         // multiple_choice / subjective / word_arrange
    private String question;
    private String korean;           // word_arrange 전용, 없으면 null
    private String tiles;            // JSON string, word_arrange 전용
    private String answerTiles;      // JSON string, word_arrange 전용
    private String distractorTiles;  // JSON string, word_arrange 전용
    private String options;          // JSON string, multiple_choice 전용
    private String answer;
    private String acceptableAnswers; // JSON string
    private boolean botIsCorrect;    // 서버가 미리 시뮬레이션한 봇 정답 여부
}