package com.qring.qring_backend.push.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.qring.qring_backend.push.service.StudyInactivityReminderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 학습 공백 푸시 스케줄러. 기본 매일 19:00 Asia/Seoul (qring.push.inactivity-reminder.cron).
 * 오답 알림(20:00)과 시각을 나눠 같은 사용자가 두 알림을 동시에 받지 않게 했다.
 * 예외는 여기서 잡아 로그로만 남긴다 — 한 번 실패해도 스케줄러 스레드가 죽지 않게.
 * qring.push.inactivity-reminder.enabled=false 로 끌 수 있다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "qring.push.inactivity-reminder", name = "enabled", havingValue = "true", matchIfMissing = true)
public class StudyInactivityReminderScheduler {

    private final StudyInactivityReminderService reminderService;

    @Scheduled(cron = "${qring.push.inactivity-reminder.cron:0 0 19 * * *}", zone = "Asia/Seoul")
    public void sendDailyReminder() {
        try {
            reminderService.runForToday();
        } catch (Exception e) {
            log.error("[PUSH] 학습 공백 알림 스케줄 실행 실패: {}", e.getMessage(), e);
        }
    }
}
