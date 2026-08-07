package com.qring.qring_backend.controller.competition;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.qring.qring_backend.domain.competition.CompetitionMatch;
import com.qring.qring_backend.dto.competition.BotLevelDto;
import com.qring.qring_backend.dto.competition.BotPauseResponseDto;
import com.qring.qring_backend.service.competition.CompetitionMatchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Bot Competition")
@RestController
@RequiredArgsConstructor
public class CompetitionMatchController {

    private final CompetitionMatchService competitionMatchService;

    @Operation(summary = "봇 컴피티션 매치 시작 - 레벨에 따른 21문제 + 봇 정답 제공, entry_cost 차감")
    @PostMapping("/bot/level")
    public ResponseEntity<BotLevelDto.Response> startMatch(
            Authentication authentication,
            @RequestBody BotLevelDto.Request request) {

        Long userId = (Long) authentication.getPrincipal();
        BotLevelDto.Response response = competitionMatchService.startMatch(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "봇 컴피티션 일시정지/재개 - True면 일시정지, False면 재시작")
    @GetMapping("/bot/pause")
    public ResponseEntity<BotPauseResponseDto> togglePause(
            Authentication authentication,
            @RequestParam("botPause") boolean botPause) {

        Long userId = (Long) authentication.getPrincipal();
        CompetitionMatch match = competitionMatchService.togglePause(userId, botPause);
        return ResponseEntity.ok(new BotPauseResponseDto(match.getMatchId(), match.getStatus().name()));
    }
}