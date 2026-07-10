package com.qring.qring_backend.controller.quiz;

import com.qring.qring_backend.dto.quiz.IncorrectResponseDto;
import com.qring.qring_backend.dto.quiz.IncorrectResultRequestDto;
import com.qring.qring_backend.dto.quiz.IncorrectResultResponseDto;
import com.qring.qring_backend.dto.quiz.IncorrectRetryResponseDto;
import com.qring.qring_backend.service.quiz.IncorrectService;
import com.qring.qring_backend.dto.quiz.IncorrectRetryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;


@RestController
@RequestMapping("/incorrect")
@RequiredArgsConstructor
public class IncorrectController {

    private final IncorrectService incorrectService;

    @PostMapping
    public ResponseEntity<IncorrectResponseDto> getWrongAnswers(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(incorrectService.getWrongAnswers(userId));
    }

    @PostMapping("/retry")
    public ResponseEntity<IncorrectRetryResponseDto> getIncorrectQuizzes(
            Authentication authentication,
            @RequestBody Map<String, Long> body) {
        Long userId = (Long) authentication.getPrincipal();
        Long contentId = body.get("contentId");
        return ResponseEntity.ok(incorrectService.getIncorrectQuizzes(userId, contentId));
    }

    @PostMapping("/result")
    public ResponseEntity<IncorrectResultResponseDto> saveIncorrectResult(
            Authentication authentication,
            @RequestBody IncorrectResultRequestDto request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(incorrectService.saveIncorrectResult(userId, request));
    }
}