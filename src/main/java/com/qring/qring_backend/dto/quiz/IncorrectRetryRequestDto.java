package com.qring.qring_backend.dto.quiz;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IncorrectRetryRequestDto {
    private String sourceType;  // STORY / COMPETITION
    private Long groupId;       // STORY: contentId / COMPETITION: level
    private Integer level;      // STORY 전용 — 해당 스토리의 어느 레벨 오답인지. null 이면 전체 레벨 (COMPETITION 은 무시)
}