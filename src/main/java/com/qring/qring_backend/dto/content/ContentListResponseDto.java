package com.qring.qring_backend.dto.content;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ContentListResponseDto {

    private Long contentId;
    private String categoryName;
    private String thumbnailUrl;
    private String title;
    private Long quizCount;
    private Boolean isCompleted;
    private String status;
    private Integer requiredPoints;
}