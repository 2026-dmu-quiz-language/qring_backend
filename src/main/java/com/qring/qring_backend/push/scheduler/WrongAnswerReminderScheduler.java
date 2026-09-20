package com.qring.qring_backend.push.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.qring.qring_backend.push.service.WrongAnswerReminderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 오답 N일차 푸시 스케줄러. 기본 매일 20:00 Asia/Seoul (qring.push.wrong-answer-reminder.cron).
 * 예외는 여기서 잡아 로그로만 남긴다 — 한 번 실패해도 스케줄러 스레드가 죽지 않게.
 * qring.push.wrong-answer-reminder.enabled=false 로 끌 수 있다 (예: 여러 인스턴스 중 하나만 돌릴 때).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "qring.push.wrong-answer-reminder", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WrongAnswerReminderScheduler {

    private final WrongAnswerReminderService reminderService;

    @Scheduled(cron = "${qring.push.wrong-answer-reminder.cron:0 0 20 * * *}", zone = "Asia/Seoul")
    public void sendDailyReminder() {
        try {
            reminderService.runForToday();
        } catch (Exception e) {
            log.error("[PUSH] 오답 알림 스케줄 실행 실패: {}", e.getMessage(), e);
        }
    }
}
