package com.qring.qring_backend.mypage.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MyPageUpdateRequest {
    private String nickname;
    private String password;
    private Boolean pushEnabled;
}
