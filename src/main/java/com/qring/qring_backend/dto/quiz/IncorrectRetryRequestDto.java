package com.qring.qring_backend.dto.quiz;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IncorrectRetryRequestDto {
    private String sourceType; // STORY / COMPETITION
    private Long groupId;      // STORY: contentId / COMPETITION: level
}