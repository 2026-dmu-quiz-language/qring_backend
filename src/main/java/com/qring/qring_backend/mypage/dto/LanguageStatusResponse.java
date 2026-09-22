package com.qring.qring_backend.mypage.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** /langcheck 와 /switch 공통 응답: 현재 학습 언어와 해금된 언어 목록. */
@Getter
@Builder
public class LanguageStatusResponse {

    /** 현재 학습 언어 코드, 서버 저장 형식 그대로 (EN / JA / ZH). 온보딩 전이면 null. */
    private String current;

    /** 현재 학습 언어 한글명 (영어 / 일본어 / 중국어). current 가 null 이면 null. */
    private String currentName;

    /** user_language_level 에 row 가 있는 언어 코드 목록, 대문자. 햄버거 메뉴의 버튼 활성화 판단에 쓴다. */
    private List<String> unlocked;
}
