package com.qring.qring_backend.dto.competition;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class BotResultDto {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class AnswerItem {
        private String sourceType; // STORY / COMPETITION
        private Long sourceQuizContentId;
        private Integer roundNo;
        private String userAnswer;
        private boolean userIsCorrect;
        private boolean botIsCorrect; // /bot/level 응답에서 받은 값 그대로 echo
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request {
        private List<AnswerItem> answers; // 21개 (맞은 문제 + 틀린 문제 전부)
        private Integer streakCount; // 연속 정답 횟수 (최대치)
    }

    @Getter
    @AllArgsConstructor
    public static class Response {
        private Long matchId;
        private int correctCount;
        private int wrongCount;
        private int rewardPoint;
        private int balanceAfter;
    }
}