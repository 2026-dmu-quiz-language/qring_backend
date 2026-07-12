package com.qring.qring_backend.mypage.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPageSettingResponse {
    private String id;
    private Boolean pushEnabled;
    private Boolean hasPassword;
}
