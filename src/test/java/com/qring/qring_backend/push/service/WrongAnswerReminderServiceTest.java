package com.qring.qring_backend.push.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.qring.qring_backend.domain.quiz.WrongAnswerReminderTarget;
import com.qring.qring_backend.domain.quiz.WrongAnswerRepository;
import com.qring.qring_backend.push.dto.WrongAnswerReminderRunResult;

/**
 * 오답 6일차 푸시: 대상 조회 창은 생성일 하루 [00:00, 다음날 00:00), 토큰 없는 사용자는 건너뛰고,
 * FCM 이 무효라고 답한 토큰은 삭제한다. 발송기는 mock — 실제 FCM 은 관리자 API 로 실기기 검증.
 */
class WrongAnswerReminderServiceTest {

    private static final LocalDate CREATED = LocalDate.of(2026, 9, 15);

    private WrongAnswerRepository wrongAnswerRepository;
    private PushTokenService pushTokenService;
    private PushSender pushSender;
    private WrongAnswerReminderService service;

    @BeforeEach
    void setUp() {
        wrongAnswerRepository = mock(WrongAnswerRepository.class);
        pushTokenService = mock(PushTokenService.class);
        pushSender = mock(PushSender.class);
        service = new WrongAnswerReminderService(wrongAnswerRepository, pushTokenService, pushSender);
        service.setDaysAfter(6);
    }

    private static WrongAnswerReminderTarget target(long userId, long count) {
        return new WrongAnswerReminderTarget() {
            @Override public Long getUserId() { return userId; }
            @Override public Long getWrongCount() { return count; }
        };
    }

    @Test
    @DisplayName("생성일 하루 창으로 조회하고, 토큰 있는 사용자마다 개수가 들어간 문구로 한 번씩 보낸다")
    void run_sendsOnePushPerUserWithTokens() {
        when(wrongAnswerRepository.findReminderTargetsCreatedBetween(
                CREATED.atStartOfDay(), CREATED.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(target(1L, 3), target(2L, 1)));
        when(pushTokenService.tokensByUser(Set.of(1L, 2L)))
                .thenReturn(Map.of(1L, List.of("t1a", "t1b"), 2L, List.of("t2")));
        when(pushSender.send(anyList(), any())).thenAnswer(inv ->
                new PushSendResult(((List<?>) inv.getArgument(0)).size(), 0, List.of()));

        WrongAnswerReminderRunResult res = service.run(CREATED);

        assertEquals(2, res.targetUsers());
        assertEquals(2, res.notifiedUsers());
        assertEquals(0, res.skippedNoToken());
        assertEquals(3, res.sentMessages());
        assertEquals(0, res.failedMessages());
        assertEquals(0, res.removedTokens());

        ArgumentCaptor<PushMessage> msg = ArgumentCaptor.forClass(PushMessage.class);
        verify(pushSender).send(eq(List.of("t1a", "t1b")), msg.capture());
        assertTrue(msg.getValue().body().contains("6일 전에 틀린 문제 3개"), msg.getValue().body());
        assertEquals("WRONG_ANSWER_REMINDER", msg.getValue().data().get("type"));
        assertEquals("incorrect", msg.getValue().data().get("screen"));
        assertEquals("3", msg.getValue().data().get("wrongCount"));
        assertEquals("2026-09-15", msg.getValue().data().get("createdDate"));
    }

    @Test
    @DisplayName("기기 토큰이 없는 사용자는 건너뛰고 발송기를 부르지 않는다")
    void run_skipsUsersWithoutTokens() {
        when(wrongAnswerRepository.findReminderTargetsCreatedBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(target(1L, 2)));
        when(pushTokenService.tokensByUser(Set.of(1L))).thenReturn(Map.of());

        WrongAnswerReminderRunResult res = service.run(CREATED);

        assertEquals(1, res.targetUsers());
        assertEquals(1, res.skippedNoToken());
        assertEquals(0, res.notifiedUsers());
        verify(pushSender, never()).send(anyList(), any());
    }

    @Test
    @DisplayName("FCM 이 무효라고 답한 토큰은 모아서 한 번에 삭제하고 결과에 반영한다")
    void run_removesInvalidTokens() {
        when(wrongAnswerRepository.findReminderTargetsCreatedBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(target(1L, 1), target(2L, 1)));
        when(pushTokenService.tokensByUser(Set.of(1L, 2L)))
                .thenReturn(Map.of(1L, List.of("dead", "alive"), 2L, List.of("dead2")));
        when(pushSender.send(eq(List.of("dead", "alive")), any()))
                .thenReturn(new PushSendResult(1, 1, List.of("dead")));
        when(pushSender.send(eq(List.of("dead2")), any()))
                .thenReturn(new PushSendResult(0, 1, List.of("dead2")));
        when(pushTokenService.removeInvalid(List.of("dead", "dead2"))).thenReturn(2);

        WrongAnswerReminderRunResult res = service.run(CREATED);

        assertEquals(1, res.notifiedUsers());   // user 2 는 성공 0건이라 알림받은 사용자로 세지 않음
        assertEquals(1, res.sentMessages());
        assertEquals(2, res.failedMessages());
        assertEquals(2, res.removedTokens());
        verify(pushTokenService).removeInvalid(List.of("dead", "dead2"));
    }

    @Test
    @DisplayName("대상이 없으면 토큰 조회·발송 없이 0 결과")
    void run_noTargets() {
        when(wrongAnswerRepository.findReminderTargetsCreatedBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        WrongAnswerReminderRunResult res = service.run(CREATED);

        assertEquals(0, res.targetUsers());
        verify(pushTokenService, never()).tokensByUser(any());
        verify(pushSender, never()).send(anyList(), any());
    }

    @Test
    @DisplayName("runForToday 는 오늘 - daysAfter 생성일을 대상으로 한다")
    void runForToday_usesDaysAfter() {
        when(wrongAnswerRepository.findReminderTargetsCreatedBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        WrongAnswerReminderRunResult res = service.runForToday();

        assertEquals(LocalDate.now().minusDays(6), res.createdDate());
        verify(wrongAnswerRepository).findReminderTargetsCreatedBetween(
                LocalDate.now().minusDays(6).atStartOfDay(), LocalDate.now().minusDays(5).atStartOfDay());
    }
}
