package com.qring.qring_backend.push.dto;

import java.time.LocalDate;

/**
 * 오답 N일차 알림 1회 실행 결과 (로그·관리자 수동 실행 응답).
 *
 * @param createdDate      대상 오답의 생성일
 * @param targetUsers      조건에 맞는 사용자 수 (푸시 켜져 있고 그 날 오답이 남아 있음)
 * @param notifiedUsers    최소 1개 기기에 성공적으로 발송된 사용자 수
 * @param skippedNoToken   등록된 기기 토큰이 없어 건너뛴 사용자 수
 * @param sentMessages     성공한 기기 단위 발송 건수
 * @param failedMessages   실패한 기기 단위 발송 건수 (무효 토큰 포함)
 * @param removedTokens    FCM 이 무효라고 답해 삭제한 토큰 수
 */
public record WrongAnswerReminderRunResult(
        LocalDate createdDate,
        int targetUsers,
        int notifiedUsers,
        int skippedNoToken,
        int sentMessages,
        int failedMessages,
        int removedTokens) {
}
