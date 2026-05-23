package com.qring.qring_backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private String name;
    private long consecutiveDays;
    private int progressRate;
    private String commentText;
    private long completedStoryCount;
    private String levelDesc;
    private Integer levelCode;
}
