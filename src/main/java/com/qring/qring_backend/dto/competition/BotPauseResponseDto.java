package com.qring.qring_backend.dto.competition;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BotPauseResponseDto {
    private Long matchId;
    private String status; // IN_PROGRESS / PAUSED
}