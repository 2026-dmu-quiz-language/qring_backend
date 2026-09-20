package com.qring.qring_backend.mypage.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPageSettingResponse {
    private String id;
    private Boolean pushEnabled;
    private Boolean hasPassword;
    /** 가입 경로: LOCAL / GOOGLE / KAKAO / LINE. */
    private String authProvider;
    /**
     * 이메일·비밀번호(LOCAL) 가입자면 true, 소셜 가입자면 false.
     * 프론트는 이 값으로 비밀번호 변경 섹션을 보이거나 숨긴다 (소셜 계정은 비밀번호 로그인이 막혀 있어 변경해도 의미 없음).
     */
    private Boolean isLocalUser;
}
