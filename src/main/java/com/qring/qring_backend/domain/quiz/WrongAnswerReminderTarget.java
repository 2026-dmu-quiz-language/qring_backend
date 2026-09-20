package com.qring.qring_backend.domain.quiz;

/** 오답 N일차 푸시 대상 — 사용자와 그 날 생긴(아직 남아 있는) 오답 개수. WrongAnswerRepository 인터페이스 프로젝션. */
public interface WrongAnswerReminderTarget {
    Long getUserId();
    Long getWrongCount();
}
