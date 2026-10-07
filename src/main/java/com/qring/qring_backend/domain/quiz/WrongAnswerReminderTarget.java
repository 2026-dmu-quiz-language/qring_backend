package com.qring.qring_backend.domain.quiz;

/**
 * 오답 N일차 푸시 대상 — 사용자와 그 날 생긴(아직 남아 있는) 오답 개수. WrongAnswerRepository 인터페이스 프로젝션.
 *
 * 오답의 언어(langCode)와 사용자의 현재 학습 언어(userLanguage)를 함께 내려주고, 같은지는 서비스에서 비교한다.
 * 쿼리 안에서 qc.lang_code = users.language 로 비교하면 운영 DB 의 두 컬럼 collation 이 달라
 * (quiz_content 는 utf8mb4_unicode_ci, users 는 utf8mb4_0900_ai_ci) "Illegal mix of collations" 로 실패했다.
 */
public interface WrongAnswerReminderTarget {
    Long getUserId();
    String getLangCode();
    String getUserLanguage();
    Long getWrongCount();
}
