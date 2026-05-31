package com.qring.qring_backend.controller.quiz;

import com.qring.qring_backend.dto.quiz.QuestionResultRequestDto;
import com.qring.qring_backend.dto.quiz.QuestionResultResponseDto;
import com.qring.qring_backend.service.quiz.QuestionResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class QuestionResultController {

    private final QuestionResultService questionResultService;

    @PostMapping("/questionResult")
    public ResponseEntity<QuestionResultResponseDto> saveQuestionResult(
            Authentication authentication,
            @RequestBody QuestionResultRequestDto request) {

        Long userId = (Long) authentication.getPrincipal();
        QuestionResultResponseDto response = questionResultService.saveResults(userId, request);
        return ResponseEntity.ok(response);
    }
}
