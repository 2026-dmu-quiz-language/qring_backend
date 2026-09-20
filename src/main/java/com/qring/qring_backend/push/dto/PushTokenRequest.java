package com.qring.qring_backend.push.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 기기 토큰 등록/해제 요청. platform 은 선택 (ANDROID / IOS / WEB). */
@Getter
@Setter
@NoArgsConstructor
public class PushTokenRequest {

    @NotBlank(message = "token 은 필수입니다.")
    @Size(max = 512, message = "token 은 512자를 넘을 수 없습니다.")
    private String token;

    @Size(max = 20)
    private String platform;
}
