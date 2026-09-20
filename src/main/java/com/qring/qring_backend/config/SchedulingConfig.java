package com.qring.qring_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** @Scheduled 활성화. 현재 사용처: push/scheduler/WrongAnswerReminderScheduler (오답 N일차 푸시). */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
