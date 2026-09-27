package com.qring.qring_backend.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class IncorrectResponseDto {

    private List<WrongAnswerItem> wrongAnswers;

    @Getter
    @AllArgsConstructor
    public static class WrongAnswerItem {
        private String sourceType;           // STORY / COMPETITION
        private Long contentId;              // STORY: content_id / COMPETITION: level (재풀이 조회 시 groupId 로 사용)
        private String label;                // STORY: storyName / COMPETITION: "레벨 N 컴피티션"
        private Integer level;               // STORY: quiz_detail.difficulty / COMPETITION: 매치 레벨
        private LocalDateTime latestWrongAt; // 정렬 기준
    }
}