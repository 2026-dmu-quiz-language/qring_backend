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
    /**
     * 학습 언어 코드, 소문자 (en / ja / zh). 마이페이지 화면의 코드→한글명 변환표가 소문자 키를 쓴다.
     * 서버 저장값은 대문자(EN/JA/ZH)라 예전엔 여기서 매칭이 안 돼 어떤 언어든 "영어"로 표시됐고,
     * 그대로 레벨을 저장하면 EN 이 전송돼 실제 언어까지 영어로 바뀌던 버그가 있었다.
     */
    private String language;
    /** 학습 언어 코드, 서버 저장 형식 그대로 (EN / JA / ZH). */
    private String languageCode;
    /** 학습 언어 한글명 (영어 / 일본어 / 중국어). 코드가 모르는 값이면 코드 그대로. */
    private String languageName;
}
