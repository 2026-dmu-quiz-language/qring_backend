package com.qring.qring_backend.service.content;

import com.qring.qring_backend.domain.content.StorySession;
import com.qring.qring_backend.domain.user.User;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 오답 재시도가 퀴즈 한도를 소모하지 않는지 검증. */
class InteractiveStoryServiceTest {

    private Map<String, Object> quiz(String question) {
        return Map.of("quiz_type", "multiple_choice", "question", question, "correct_answer", "green tea");
    }

    @Test
    @DisplayName("같은 문제를 다시 낸 경우 재시도로 판별한다 (공백·대소문자 무시)")
    void detectsRetryIgnoringWhitespaceAndCase() {
        Map<String, Object> pending = quiz("'녹차'를 뜻하는 표현은?");

        assertTrue(InteractiveStoryService.isSameQuestion(pending, quiz("'녹차'를 뜻하는 표현은?")));
        assertTrue(InteractiveStoryService.isSameQuestion(pending, quiz("  '녹차'를  뜻하는   표현은?  ")));
        assertFalse(InteractiveStoryService.isSameQuestion(pending, quiz("'홍차'를 뜻하는 표현은?")));
    }

    @Test
    @DisplayName("일본어 세션에서도 같은 표현을 다시 출제하면 기출 중복으로 잡는다")
    void detectsDuplicateSubjectInNoSpaceLanguage() {
        // 실측(2026-09-22): "続けてベットします" 를 내고 다시 "ベットを続けます" 를 냈다
        assertTrue(InteractiveStoryService.isDuplicateQuizSubject(
                List.of("続けてベットします"), Map.of("correct_answer", "ベットを続けます"), "Japanese"));
        assertFalse(InteractiveStoryService.isDuplicateQuizSubject(
                List.of("続けてベットします"), Map.of("correct_answer", "カードを引きます"), "Japanese"));
        assertFalse(InteractiveStoryService.isDuplicateQuizSubject(
                List.of("続けてベットします"), Map.of("correct_answer", "ベットを続けます"), "English"),
                "영어 규칙은 낱말 단위 그대로다");
    }

    @Test
    @DisplayName("이어하기는 생성 중인 턴을 기다리고, 제한 시간을 넘기면 진행 중으로 알린다")
    void awaitsTurnInFlightBeforeAnswering() throws Exception {
        CountDownLatch finished = new CountDownLatch(1);
        finished.countDown();
        assertFalse(InteractiveStoryService.awaitTurn(finished, Duration.ofMillis(50)),
                "이미 끝난 턴은 기다리지 않는다");

        CountDownLatch stuck = new CountDownLatch(1);
        long startedAt = System.nanoTime();
        assertTrue(InteractiveStoryService.awaitTurn(stuck, Duration.ofMillis(120)),
                "제한 시간 안에 끝나지 않으면 아직 진행 중으로 본다");
        assertTrue(System.nanoTime() - startedAt >= 90_000_000L, "제한 시간만큼은 기다려야 한다");

        // 실측 사고(2026-09-22): 생성 중에 앱을 나갔다 들어오면 답 없는 내 메시지로 끝난 대화가 내려갔다
        CountDownLatch finishesSoon = new CountDownLatch(1);
        Thread turn = new Thread(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            finishesSoon.countDown();
        });
        turn.start();
        assertFalse(InteractiveStoryService.awaitTurn(finishesSoon, Duration.ofSeconds(3)),
                "턴이 끝나면 완성된 대화를 돌려준다");
        turn.join();
    }

    @Test
    @DisplayName("세션 언어는 사용자 설정(users.language)으로 정하고 요청의 targetLanguage 는 무시한다")
    void resolvesTargetLanguageFromUserNotRequest() {
        User japanese = User.builder().userId(1L).email("ja@example.com").language("JA").build();
        assertEquals("Japanese", InteractiveStoryService.resolveTargetLanguage(japanese, "English"),
                "프론트가 English 를 보내도 사용자 설정이 우선이다");
        assertEquals("Japanese", InteractiveStoryService.resolveTargetLanguage(japanese, null));

        User chinese = User.builder().userId(2L).email("zh@example.com").language("zh").build();
        assertEquals("Chinese (Simplified)", InteractiveStoryService.resolveTargetLanguage(chinese, null));

        User notOnboarded = User.builder().userId(3L).email("new@example.com").build();
        assertEquals("English", InteractiveStoryService.resolveTargetLanguage(notOnboarded, "Japanese"),
                "언어 미설정 사용자는 기본값 English (요청값으로 올리지 않는다)");

        User unknownCode = User.builder().userId(4L).email("x@example.com").language("KO").build();
        assertEquals("English", InteractiveStoryService.resolveTargetLanguage(unknownCode, null));
    }

    @Test
    @DisplayName("대기 중인 퀴즈가 없으면 재시도가 아니다")
    void noPendingQuizMeansNoRetry() {
        assertFalse(InteractiveStoryService.isSameQuestion(null, quiz("'녹차'를 뜻하는 표현은?")));
        assertFalse(InteractiveStoryService.isSameQuestion(quiz("'녹차'를 뜻하는 표현은?"), null));
    }

    @Test
    @DisplayName("재시도는 퀴즈 카운트를 늘리지 않고 대기 퀴즈를 유지한다")
    void retryDoesNotConsumeQuizBudget() {
        StorySession session = StorySession.builder()
                .sessionId("s").userId(1L).characterName("지민")
                .situationDescription("카페").tone("다정하게")
                .targetLanguage("English").levelCode(1).build();

        Map<String, Object> first = quiz("'녹차'를 뜻하는 표현은?");
        session.recordQuiz(first);
        assertEquals(1, session.getQuizCount());

        // 오답 후 같은 문제를 다시 제시
        session.incrementTurnsSinceLastQuiz();
        session.repeatPendingQuiz();

        assertEquals(1, session.getQuizCount(), "재시도가 퀴즈 한도를 소모하면 안 된다");
        assertEquals(0, session.getTurnsSinceLastQuiz(), "재시도도 퀴즈 턴이므로 페이싱은 초기화된다");
        assertSame(first, session.getPendingQuiz(), "재시도 중에는 같은 퀴즈가 계속 채점 대상이어야 한다");
    }

    @Test
    @DisplayName("오답 횟수는 퀴즈마다 누적되고, 새 퀴즈 출제나 채점 종료 시 0으로 돌아간다")
    void wrongAttemptsAreTrackedPerQuiz() {
        StorySession session = StorySession.builder()
                .sessionId("s").userId(1L).characterName("지민")
                .situationDescription("카페").tone("다정하게")
                .targetLanguage("English").levelCode(1).build();

        session.recordQuiz(quiz("'녹차'를 뜻하는 표현은?"));
        assertEquals(1, session.recordWrongAttempt());
        assertEquals(2, session.recordWrongAttempt());
        assertEquals(3, session.recordWrongAttempt());
        assertTrue(session.getWrongAttempts() >= OpenAiStoryService.MAX_WRONG_ATTEMPTS);

        session.clearPendingQuiz();
        assertEquals(0, session.getWrongAttempts());

        session.recordWrongAttempt();
        session.recordQuiz(quiz("'홍차'를 뜻하는 표현은?"));
        assertEquals(0, session.getWrongAttempts(), "새 퀴즈가 나오면 오답 횟수는 초기화된다");
    }

    @Test
    @DisplayName("퀴즈 5개를 모두 채점하면 서버가 세션 종료를 확정한다")
    void forcesCompletionAfterAllQuizzesGraded() {
        StorySession session = StorySession.builder()
                .sessionId("s").userId(1L).characterName("지민")
                .situationDescription("카페").tone("다정하게")
                .targetLanguage("English").levelCode(1).build();

        for (int i = 0; i < OpenAiStoryService.MAX_QUIZ_COUNT - 1; i++) {
            session.recordQuiz(quiz("문제 " + i));
            session.clearPendingQuiz();
        }
        assertFalse(InteractiveStoryService.shouldForceComplete(session),
                "퀴즈가 남아 있으면 강제 종료하면 안 된다");

        session.recordQuiz(quiz("마지막 문제"));
        assertFalse(InteractiveStoryService.shouldForceComplete(session),
                "마지막 퀴즈가 채점 대기 중이면 아직 종료하면 안 된다");

        session.clearPendingQuiz();
        assertTrue(InteractiveStoryService.shouldForceComplete(session),
                "5개 모두 채점이 끝나면 서버가 종료를 확정해야 한다");
    }

    @Test
    @DisplayName("이어하기: 한도가 늘고 완결이 해제되며, 늘어난 한도까지는 강제 종료되지 않는다")
    void extensionRaisesLimitAndReopensSession() {
        StorySession session = StorySession.builder()
                .sessionId("s").userId(1L).characterName("지민")
                .situationDescription("카페").tone("다정하게")
                .targetLanguage("English").levelCode(1).build();

        for (int i = 0; i < StorySession.DEFAULT_QUIZ_LIMIT; i++) {
            session.recordQuiz(quiz("문제 " + i));
            session.clearPendingQuiz();
        }
        session.setCompleted(true);
        assertTrue(InteractiveStoryService.shouldForceComplete(session));

        session.extendQuizLimit(InteractiveStoryService.EXTEND_QUIZ_COUNT);

        assertFalse(session.isCompleted(), "이어하기 후에는 완결이 해제되어야 한다");
        assertEquals(10, session.getQuizLimit());
        assertEquals(0, session.getTurnsSinceLastQuiz(), "연장 직후엔 잠시 대화가 이어지도록 페이싱 초기화");
        assertFalse(InteractiveStoryService.shouldForceComplete(session),
                "늘어난 한도(10)를 채우기 전에는 강제 종료되면 안 된다");

        for (int i = 5; i < 10; i++) {
            session.recordQuiz(quiz("문제 " + i));
            session.clearPendingQuiz();
        }
        assertTrue(InteractiveStoryService.shouldForceComplete(session),
                "연장된 한도를 다 채우면 다시 종료 대상이 된다");
    }

    @Test
    @DisplayName("이어하기는 최대 2회 — 한도 15에 도달하면 더 연장할 수 없다")
    void extensionIsCappedAtTwo() {
        StorySession session = StorySession.builder()
                .sessionId("s").userId(1L).characterName("지민")
                .situationDescription("카페").tone("다정하게")
                .targetLanguage("English").levelCode(1).build();

        assertTrue(InteractiveStoryService.canExtend(session), "기본 상태(한도 5)는 연장 가능");

        session.extendQuizLimit(InteractiveStoryService.EXTEND_QUIZ_COUNT); // 10
        assertTrue(InteractiveStoryService.canExtend(session), "1회 연장 후(한도 10)에도 연장 가능");

        session.extendQuizLimit(InteractiveStoryService.EXTEND_QUIZ_COUNT); // 15
        assertFalse(InteractiveStoryService.canExtend(session), "2회 연장 후(한도 15)에는 더 연장할 수 없다");
        assertEquals(InteractiveStoryService.MAX_QUIZ_LIMIT, session.getQuizLimit());
    }

    @Test
    @DisplayName("이미 다룬 표현을 형식만 바꿔 다시 낸 퀴즈를 중복으로 판별한다")
    void detectsDuplicateSubjectAcrossQuizFormats() {
        List<String> tested = List.of("green tea", "relax", "nervous", "rest");

        // 같은 표현을 주관식으로 재출제 (대소문자·문장부호 무시)
        assertTrue(InteractiveStoryService.isDuplicateQuizSubject(tested, Map.of(
                "quiz_type", "subjective", "question", "'녹차'를 영어로?",
                "acceptable_answers", List.of("Green Tea!"))));

        // 기출 단어에 한 단어만 붙인 답안은 같은 표현의 재출제
        assertTrue(InteractiveStoryService.isDuplicateQuizSubject(tested, Map.of(
                "quiz_type", "multiple_choice", "question", "'아이스 녹차'를 뜻하는 표현은?",
                "correct_answer", "iced green tea")));
        assertTrue(InteractiveStoryService.isDuplicateQuizSubject(tested, Map.of(
                "quiz_type", "subjective", "question", "'긴장돼'를 뜻하는 표현을 입력하세요",
                "acceptable_answers", List.of("I'm nervous"))));

        // 긴 문장 안에 기출 단어 하나가 들어간 단어 배열은 문장 자체를 새로 묻는 것이므로 통과
        assertFalse(InteractiveStoryService.isDuplicateQuizSubject(tested, Map.of(
                "quiz_type", "word_arrange", "question", "문장을 배열하세요",
                "correct_answer", "I want to relax at home")));

        // 부분 문자열 매칭은 하지 않는다 ("rest" 기출이 "restaurant"를 막으면 안 된다)
        assertFalse(InteractiveStoryService.isDuplicateQuizSubject(tested, Map.of(
                "quiz_type", "multiple_choice", "question", "'식당'을 뜻하는 표현은?",
                "correct_answer", "restaurant")));

        // 완전히 새로운 표현은 통과
        assertFalse(InteractiveStoryService.isDuplicateQuizSubject(tested, Map.of(
                "quiz_type", "multiple_choice", "question", "'창가 자리'를 뜻하는 표현은?",
                "correct_answer", "window seat")));

        // 기출 목록이 비어 있으면 항상 통과
        assertFalse(InteractiveStoryService.isDuplicateQuizSubject(List.of(), Map.of(
                "correct_answer", "green tea")));
    }

    @Test
    @DisplayName("새 퀴즈는 정상적으로 카운트를 늘린다")
    void newQuizConsumesBudget() {
        StorySession session = StorySession.builder()
                .sessionId("s").userId(1L).characterName("지민")
                .situationDescription("카페").tone("다정하게")
                .targetLanguage("English").levelCode(1).build();

        session.recordQuiz(quiz("'녹차'를 뜻하는 표현은?"));
        session.recordQuiz(quiz("'홍차'를 뜻하는 표현은?"));

        assertEquals(2, session.getQuizCount());
    }
}
