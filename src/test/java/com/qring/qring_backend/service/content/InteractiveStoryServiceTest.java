package com.qring.qring_backend.service.content;

import com.qring.qring_backend.domain.content.StorySession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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
        List<String> tested = List.of("green tea", "relax");

        // 같은 표현을 주관식으로 재출제
        assertTrue(InteractiveStoryService.isDuplicateQuizSubject(tested, Map.of(
                "quiz_type", "subjective", "question", "'녹차'를 영어로?",
                "acceptable_answers", List.of("Green Tea"))));

        // 같은 표현이 단어 배열 정답 문장 안에 포함됨
        assertTrue(InteractiveStoryService.isDuplicateQuizSubject(tested, Map.of(
                "quiz_type", "word_arrange", "question", "문장을 배열하세요",
                "correct_answer", "I want to relax at home")));

        // 완전히 새로운 표현은 통과
        assertFalse(InteractiveStoryService.isDuplicateQuizSubject(tested, Map.of(
                "quiz_type", "multiple_choice", "question", "'긴장돼'를 뜻하는 표현은?",
                "correct_answer", "nervous")));

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
