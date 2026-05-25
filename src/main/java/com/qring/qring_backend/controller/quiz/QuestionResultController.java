package com.qring.qring_backend.controller.quiz;

import com.qring.qring_backend.auth.security.JwtTokenProvider;
import com.qring.qring_backend.dto.quiz.QuestionResultRequestDto;
import com.qring.qring_backend.dto.quiz.QuestionResultResponseDto;
import com.qring.qring_backend.service.quiz.QuestionResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class QuestionResultController {

    private final QuestionResultService questionResultService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/questionResult")
    public ResponseEntity<QuestionResultResponseDto> saveQuestionResult(
            @RequestHeader("Authorization") String token,
            @RequestBody QuestionResultRequestDto request) {

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        QuestionResultResponseDto response = questionResultService.saveResult(userId, request);
        return ResponseEntity.ok(response);
    }
}