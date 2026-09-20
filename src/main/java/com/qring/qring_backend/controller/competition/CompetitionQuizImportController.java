package com.qring.qring_backend.controller.competition;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.security.core.Authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qring.qring_backend.auth.security.AdminAccessGuard;
import com.qring.qring_backend.dto.competition.CompetitionQuizImportDto;
import com.qring.qring_backend.service.competition.CompetitionQuizImportService;
import com.qring.qring_backend.service.competition.CompetitionQuizImportService.ImportResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/** 봇 컴피티션 문제 json 업로드용 (영우 오빠가 주는 언어별 json 등록) */
@Tag(name = "Competition Quiz Import")
@RestController
@RequestMapping("/admin/competition/quiz")
@RequiredArgsConstructor
public class CompetitionQuizImportController {

    private final CompetitionQuizImportService importService;
    private final AdminAccessGuard adminAccessGuard;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * quizSet: 세트 파일 구분자 (예: competition_quiz_ja_02.json → "02"). 같은 세트의 언어별 파일은 같은 값으로 올린다.
     * 세트마다 id 가 1 부터 시작하므로 다른 세트를 같은 quizSet 으로 올리면 두 번째 파일은 전부 "이미 있음" 으로 건너뛴다.
     */
    @Operation(summary = "봇 컴피티션 문제 json 업로드 (언어별, quizSet 으로 세트 구분) — ADMIN_USER_IDS 에 등록된 관리자만")
    @PostMapping(value = "/import", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResult> importQuizSet(Authentication authentication,
                                                     @RequestParam("file") MultipartFile file,
                                                     @RequestParam(value = "quizSet", defaultValue = "default") String quizSet)
            throws Exception {
        adminAccessGuard.checkAdmin((Long) authentication.getPrincipal());
        CompetitionQuizImportDto dto = objectMapper.readValue(file.getInputStream(), CompetitionQuizImportDto.class);
        ImportResult result = importService.importQuizSet(dto, quizSet);
        return ResponseEntity.ok(result);
    }
}