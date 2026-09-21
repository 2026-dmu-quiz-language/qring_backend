package com.qring.qring_backend.dto.quiz;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class IncorrectResultRequestDto {

    private String sourceType;   // 재풀이 화면 구분: STORY / COMPETITION (이 값에 따라 어느 리포지토리에서 지울지 결정)
    private Long contentId;      // STORY: content_id / COMPETITION: level
    private List<QuizResultDto> results;

    @Getter
    @NoArgsConstructor
    public static class QuizResultDto {
        private Long quizContentId;
        private boolean correct;
        private String originSourceType; // COMPETITION 버킷일 때만 사용: 그 문제의 원본이 STORY/COMPETITION 인지
    }
}