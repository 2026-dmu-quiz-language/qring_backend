package com.qring.qring_backend.mypage.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MyPageLearningRequest {
    private String language;
    private Integer levelCode;
}
