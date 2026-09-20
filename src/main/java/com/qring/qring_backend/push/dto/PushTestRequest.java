package com.qring.qring_backend.push.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 관리자 테스트 발송 요청. title/body 를 비우면 기본 문구. */
@Getter
@Setter
@NoArgsConstructor
public class PushTestRequest {

    @NotNull(message = "userId 는 필수입니다.")
    private Long userId;

    private String title;
    private String body;
}
