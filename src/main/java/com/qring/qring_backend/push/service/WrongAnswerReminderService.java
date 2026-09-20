package com.qring.qring_backend.push.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.qring.qring_backend.domain.quiz.WrongAnswerReminderTarget;
import com.qring.qring_backend.domain.quiz.WrongAnswerRepository;
import com.qring.qring_backend.push.dto.WrongAnswerReminderRunResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 오답 N일차 푸시 (설계: PUSH_NOTIFICATION_DESIGN.md).
 *
 * 오답(wrong_answer)은 다시 풀면 지워지고, 7일이 지나면 오답 노트에서 보이지 않는다.
 * 그래서 "생성 6일차" 저녁에 아직 남아 있는 오답이 있는 사용자에게 한 번 알린다 — 내일이면 사라지니 지금 풀라는 뜻.
 *
 * 대상 판정은 DB 한 번(findReminderTargetsCreatedBetween), 토큰 조회 한 번, 그 뒤 사용자별 발송.
 * 트랜잭션을 걸지 않는다 — 발송은 외부 네트워크 호출이라 DB 커넥션을 붙잡고 있을 이유가 없다.
 * 하루 한 번만 도는 배치이고 대상 조회 창이 하루라 같은 오답으로 두 번 보내지 않는다.
 *
 * 날짜 창은 서버 JVM 기본 시간대의 LocalDateTime — wrong_answer.created_at 이 @CreationTimestamp 로
 * 같은 시간대에 기록되므로 그것과 맞춘 것이다 (Dockerfile 이 Asia/Seoul 로 고정).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WrongAnswerReminderService {

    public static final String DATA_TYPE = "WRONG_ANSWER_REMINDER";
    /** 앱이 알림 탭 시 이동할 화면 식별자. 프론트가 이 키를 읽어 오답 노트로 라우팅하도록 협의 필요. */
    public static final String DATA_SCREEN = "incorrect";

    private final WrongAnswerRepository wrongAnswerRepository;
    private final PushTokenService pushTokenService;
    private final PushSender pushSender;

    @Value("${qring.push.wrong-answer-reminder.days-after:6}")
    private int daysAfter;

    /** 스케줄러 진입점: 오늘 기준 daysAfter 일 전에 생긴 오답을 대상으로. */
    public WrongAnswerReminderRunResult runForToday() {
        return run(LocalDate.now().minusDays(daysAfter));
    }

    /** 특정 생성일(createdDate) 의 오답을 대상으로 발송. 관리자 수동 실행·테스트에서도 이 메서드를 쓴다. */
    public WrongAnswerReminderRunResult run(LocalDate createdDate) {
        LocalDateTime start = createdDate.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        List<WrongAnswerReminderTarget> targets = wrongAnswerRepository.findReminderTargetsCreatedBetween(start, end);
        if (targets.isEmpty()) {
            log.info("[PUSH] 오답 {}일차 알림: {} 생성 오답 대상 없음", daysAfter, createdDate);
            return new WrongAnswerReminderRunResult(createdDate, 0, 0, 0, 0, 0, 0);
        }

        // LinkedHashMap 으로 대상 순서를 유지 (로그·결과 재현성)
        Map<Long, Long> countByUser = new LinkedHashMap<>();
        for (WrongAnswerReminderTarget t : targets) {
            countByUser.put(t.getUserId(), t.getWrongCount());
        }
        Map<Long, List<String>> tokensByUser = pushTokenService.tokensByUser(countByUser.keySet());

        int notifiedUsers = 0;
        int skippedNoToken = 0;
        int sent = 0;
        int failed = 0;
        List<String> invalid = new ArrayList<>();

        for (Map.Entry<Long, Long> e : countByUser.entrySet()) {
            List<String> tokens = tokensByUser.getOrDefault(e.getKey(), List.of());
            if (tokens.isEmpty()) {
                skippedNoToken++;
                continue;
            }
            PushSendResult r = pushSender.send(tokens, buildMessage(e.getValue(), createdDate));
            sent += r.successCount();
            failed += r.failureCount();
            invalid.addAll(r.invalidTokens());
            if (r.successCount() > 0) {
                notifiedUsers++;
            }
        }

        int removed = pushTokenService.removeInvalid(invalid);

        WrongAnswerReminderRunResult result = new WrongAnswerReminderRunResult(
                createdDate, countByUser.size(), notifiedUsers, skippedNoToken, sent, failed, removed);
        log.info("[PUSH] 오답 {}일차 알림 완료: {}", daysAfter, result);
        return result;
    }

    /** 알림 문구. 개수는 그 날 생긴 오답 중 아직 남은 것만 센 값. */
    PushMessage buildMessage(long wrongCount, LocalDate createdDate) {
        String title = "오답 노트가 기다리고 있어요 📝";
        String body = daysAfter + "일 전에 틀린 문제 " + wrongCount + "개가 아직 남아 있어요. "
                + "내일이면 오답 노트에서 사라지니 지금 복습해 보세요!";
        Map<String, String> data = new LinkedHashMap<>();
        data.put("type", DATA_TYPE);
        data.put("screen", DATA_SCREEN);
        data.put("wrongCount", String.valueOf(wrongCount));
        data.put("createdDate", createdDate.toString());
        return new PushMessage(title, body, data);
    }

    /* 테스트 지원 */
    void setDaysAfter(int daysAfter) {
        this.daysAfter = daysAfter;
    }
}
