package com.qring.qring_backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 대시보드 응답 — 닉네임, 연속 학습일, 진도율(%), 성취 코멘트, 완료 스토리 수, 레벨 정보, 주간 학습 현황. */
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
    /** 이번 주 월~일 학습 여부. 길이 7, [0]=월 ~ [6]=일. */
    private boolean[] weeklyStudy;
}
