package com.qring.qring_backend.controller.competition;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Operation(summary = "봇 컴피티션 문제 json 업로드 (언어별)")
    @PostMapping(value = "/import", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResult> importQuizSet(@RequestParam("file") MultipartFile file) throws Exception {
        CompetitionQuizImportDto dto = objectMapper.readValue(file.getInputStream(), CompetitionQuizImportDto.class);
        ImportResult result = importService.importQuizSet(dto);
        return ResponseEntity.ok(result);
    }
}