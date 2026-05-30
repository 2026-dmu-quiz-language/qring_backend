package com.qring.qring_backend.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuestionResultResponseDto {

    private int totalScore;
    private int correctCount;
}