package com.qring.qring_backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 대시보드 응답 — 닉네임, 연속 학습일, 진도율(%), 성취 코멘트, 완료 스토리 수, 레벨 정보. */
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
