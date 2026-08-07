package com.qring.qring_backend.dto.competition;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class BotLevelDto {

    @Getter @Setter @NoArgsConstructor
    public static class Request {
        private String botLevel;   // 상 / 중 / 하
        private Integer entryCost; // 프론트가 계산해서 보내는 입장 비용
    }

    @Getter
    @AllArgsConstructor
    public static class Response {
        private Long matchId;
        private List<CompetitionQuizItemDto> questions; // 21문제
        private Integer remainingPoints; // 차감 후 잔여 포인트
    }
}