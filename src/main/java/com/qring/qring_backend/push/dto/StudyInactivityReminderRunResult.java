package com.qring.qring_backend.push.dto;

import java.time.LocalDate;

/**
 * 학습 공백 알림 1회 실행 결과 (로그·관리자 수동 실행 응답).
 *
 * @param lastStudyDate    대상 사용자의 마지막 학습일 (이 날 이후 학습 기록이 없는 사용자에게 보낸다)
 * @param targetUsers      조건에 맞는 사용자 수 (푸시 켜져 있고 마지막 학습일이 lastStudyDate)
 * @param notifiedUsers    최소 1개 기기에 성공적으로 발송된 사용자 수
 * @param skippedNoToken   등록된 기기 토큰이 없어 건너뛴 사용자 수
 * @param sentMessages     성공한 기기 단위 발송 건수
 * @param failedMessages   실패한 기기 단위 발송 건수 (무효 토큰 포함)
 * @param removedTokens    발송 제공자가 무효라고 답해 삭제한 토큰 수
 */
public record StudyInactivityReminderRunResult(
        LocalDate lastStudyDate,
        int targetUsers,
        int notifiedUsers,
        int skippedNoToken,
        int sentMessages,
        int failedMessages,
        int removedTokens) {
}
