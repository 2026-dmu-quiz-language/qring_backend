package com.qring.qring_backend.push.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.qring.qring_backend.domain.user.UserStudyLogRepository;
import com.qring.qring_backend.push.dto.StudyInactivityReminderRunResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 학습 공백 푸시 (설계: PUSH_NOTIFICATION_DESIGN.md).
 *
 * 마지막 학습일로부터 days 일이 지난 날, 그동안 학습 기록이 없는 사용자에게 한 번 알린다.
 * 학습 기준은 대시보드 연속 학습일(StudyStreakService)과 같은 user_study_log — 스토리 퀴즈·봇 컴피티션 완료 시 쌓인다.
 *
 * 대상은 "마지막 학습일이 정확히 오늘 - days" 인 사용자라서 별도 발송 기록 없이도 한 공백 기간에 한 번만 나간다.
 * 그 뒤로도 계속 학습하지 않으면 추가 알림은 없고, 다시 학습했다가 또 days 일 쉬면 그때 다시 나간다.
 * 학습 기록이 한 번도 없는 사용자(가입만 한 사용자)는 대상이 아니다.
 *
 * 트랜잭션을 걸지 않는 이유와 토큰 처리 방식은 WrongAnswerReminderService 와 같다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudyInactivityReminderService {

    public static final String DATA_TYPE = "STUDY_INACTIVITY_REMINDER";
    /** 앱이 알림 탭 시 이동할 화면 식별자. 프론트가 이 값을 처리하지 않으면 앱만 열린다. */
    public static final String DATA_SCREEN = "home";

    private final UserStudyLogRepository userStudyLogRepository;
    private final PushTokenService pushTokenService;
    private final PushSender pushSender;

    @Value("${qring.push.inactivity-reminder.days:7}")
    private int days;

    /** 스케줄러 진입점: 마지막 학습일이 오늘 - days 인 사용자를 대상으로. */
    public StudyInactivityReminderRunResult runForToday() {
        return run(LocalDate.now().minusDays(days));
    }

    /** 마지막 학습일이 lastStudyDate 인 사용자에게 발송. 관리자 수동 실행·테스트에서도 이 메서드를 쓴다. */
    public StudyInactivityReminderRunResult run(LocalDate lastStudyDate) {
        LocalDateTime start = lastStudyDate.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        List<Long> userIds = userStudyLogRepository.findUserIdsLastStudiedBetween(start, end);
        if (userIds.isEmpty()) {
            log.info("[PUSH] 학습 공백 {}일 알림: 마지막 학습일 {} 대상 없음", days, lastStudyDate);
            return new StudyInactivityReminderRunResult(lastStudyDate, 0, 0, 0, 0, 0, 0);
        }

        Map<Long, List<String>> tokensByUser = pushTokenService.tokensByUser(userIds);
        PushMessage message = buildMessage(lastStudyDate);

        int notifiedUsers = 0;
        int skippedNoToken = 0;
        int sent = 0;
        int failed = 0;
        List<String> invalid = new ArrayList<>();

        for (Long userId : userIds) {
            List<String> tokens = tokensByUser.getOrDefault(userId, List.of());
            if (tokens.isEmpty()) {
                skippedNoToken++;
                continue;
            }
            PushSendResult r = pushSender.send(tokens, message);
            sent += r.successCount();
            failed += r.failureCount();
            invalid.addAll(r.invalidTokens());
            if (r.successCount() > 0) {
                notifiedUsers++;
            }
        }

        int removed = pushTokenService.removeInvalid(invalid);

        StudyInactivityReminderRunResult result = new StudyInactivityReminderRunResult(
                lastStudyDate, userIds.size(), notifiedUsers, skippedNoToken, sent, failed, removed);
        log.info("[PUSH] 학습 공백 {}일 알림 완료: {}", days, result);
        return result;
    }

    /** 알림 문구. 사용자별 내용이 같아 한 번만 만든다. */
    PushMessage buildMessage(LocalDate lastStudyDate) {
        String title = "Qring 이 기다리고 있어요 📚";
        String body = days + "일 동안 학습 기록이 없어요. 오늘 짧게라도 이어서 학습해 볼까요?";
        Map<String, String> data = new LinkedHashMap<>();
        data.put("type", DATA_TYPE);
        data.put("screen", DATA_SCREEN);
        data.put("lastStudyDate", lastStudyDate.toString());
        return new PushMessage(title, body, data);
    }
}
