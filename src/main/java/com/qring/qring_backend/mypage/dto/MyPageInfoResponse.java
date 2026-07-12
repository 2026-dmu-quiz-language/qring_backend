package com.qring.qring_backend.mypage.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPageInfoResponse {
    private String nickname;
    private Integer levelCode;
    private String levelDesc;
    private Integer points;
    private Long consecutiveDays;
    private String language;
}
