package com.qring.qring_backend.service.content;

import com.qring.qring_backend.domain.content.StorySession;
import com.qring.qring_backend.domain.content.StorySessionEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/** 세션 직렬화/복원 및 타임라인 순서 검증 — 다시 열람할 때 퀴즈가 대화 중간의 제자리에 있어야 한다. */
class StorySessionMapperTest {

    private final StorySessionMapper mapper = new StorySessionMapper();

    private static final Map<String, Object> QUIZ = Map.of(
            "quiz_number", 1, "quiz_type", "multiple_choice",
            "question", "'녹차'를 뜻하는 표현은?",
            "options", List.of("green tea", "black coffee"),
            "correct_answer", "green tea");

    /** 오프닝 → 잡담 → 퀴즈 출제 → 오답 → 재출제 → 정답 흐름을 실제 서비스 순서대로 재현. */
    private StorySession playedSession() {
        StorySession session = StorySession.builder()
                .sessionId("sess-roundtrip")
                .userId(7L)
                .characterName("지민")
                .situationDescription("카페에서 친구와 수다 떠는 상황")
                .tone("다정하게")
                .targetLanguage("English")
                .levelCode(2)
                .build();

        session.addAssistantMessage("Hey! What do you want to drink?", "야! 뭐 마실래?");
        session.addMessage("user", "Something warm, please.");
        session.incrementTurnsSinceLastQuiz();
        session.addTestedQuizSubject("green tea");
        session.recordQuiz(QUIZ);
        session.addAssistantMessage("How about green tea? Pick the right word!", "녹차 어때? 맞는 표현을 골라봐!");
        session.addQuizPresented(QUIZ);

        session.addMessage("user", "black coffee");
        session.incrementTurnsSinceLastQuiz();
        session.addQuizResult(QUIZ, "black coffee", "incorrect");
        session.repeatPendingQuiz();
        session.addAssistantMessage("That's black coffee! 녹차는 'green tea'야.", "그건 블랙커피잖아! 녹차는 'green tea'야.");
        session.addQuizPresented(QUIZ);

        session.addMessage("user", "green tea");
        session.incrementTurnsSinceLastQuiz();
        session.addQuizResult(QUIZ, "green tea", "correct");
        session.clearPendingQuiz();
        session.addAssistantMessage("Green tea it is!", "녹차 좋지!");
        return session;
    }

    @Test
    @DisplayName("타임라인에 대화와 퀴즈가 실제 진행 순서 그대로 섞여 기록된다")
    void timelinePreservesInterleavedOrder() {
        List<Map<String, Object>> timeline = playedSession().getTimeline();

        String[] expectedTypes = {
                "message",      // assistant 오프닝
                "message",      // user 잡담
                "message",      // assistant 퀴즈 출제 대사
                "quiz",         // 퀴즈 카드
                "message",      // user 오답 제출
                "quiz_result",  // incorrect
                "message",      // assistant 오답 반응 + 재출제 대사
                "quiz",         // 같은 퀴즈 재출제
                "message",      // user 정답 제출
                "quiz_result",  // correct
                "message"       // assistant 정답 반응
        };
        assertEquals(expectedTypes.length, timeline.size());
        for (int i = 0; i < expectedTypes.length; i++) {
            assertEquals(expectedTypes[i], timeline.get(i).get("type"), "타임라인 " + i + "번째 이벤트 타입");
        }

        assertEquals("incorrect", timeline.get(5).get("result"));
        assertEquals("correct", timeline.get(9).get("result"));
        assertEquals("야! 뭐 마실래?", timeline.get(0).get("translation"), "assistant 이벤트에는 번역이 함께 저장되어야 한다");
    }

    @Test
    @DisplayName("세션을 엔티티로 저장했다가 복원해도 타임라인과 진행 상태가 그대로 유지된다")
    void roundTripPreservesFullState() {
        StorySession original = playedSession();

        StorySessionEntity entity = mapper.toNewEntity(original);
        StorySession restored = mapper.toDomain(entity);

        assertEquals(original.getSessionId(), restored.getSessionId());
        assertEquals(original.getUserId(), restored.getUserId());
        assertEquals(original.getLevelCode(), restored.getLevelCode());
        assertEquals(original.getQuizCount(), restored.getQuizCount());
        assertEquals(original.getTurnsSinceLastQuiz(), restored.getTurnsSinceLastQuiz());
        assertEquals(original.getChatHistory(), restored.getChatHistory());
        assertEquals(original.getTestedQuizSubjects(), restored.getTestedQuizSubjects());
        assertEquals(original.getUsedQuizTypes(), restored.getUsedQuizTypes());
        assertEquals(original.getTimeline().size(), restored.getTimeline().size());
        for (int i = 0; i < original.getTimeline().size(); i++) {
            assertEquals(original.getTimeline().get(i).get("type"), restored.getTimeline().get(i).get("type"),
                    "복원 후 타임라인 " + i + "번째 이벤트 순서가 달라지면 안 된다");
        }
        assertNull(restored.getPendingQuiz());
    }

    @Test
    @DisplayName("채점 대기 중에 저장된 세션은 복원 후에도 같은 퀴즈가 채점 대상이다")
    void pendingQuizSurvivesRoundTrip() {
        StorySession session = playedSession();
        session.recordQuiz(Map.of("quiz_number", 2, "quiz_type", "subjective",
                "question", "'긴장돼'를 뜻하는 표현은?", "acceptable_answers", List.of("nervous")));

        StorySession restored = mapper.toDomain(mapper.toNewEntity(session));

        assertNotNull(restored.getPendingQuiz());
        assertEquals("'긴장돼'를 뜻하는 표현은?", restored.getPendingQuiz().get("question"));
    }

    @Test
    @DisplayName("OpenAI 실패 롤백 시 타임라인의 사용자 메시지도 함께 제거된다")
    void rollbackRemovesTimelineUserMessage() {
        StorySession session = playedSession();
        int before = session.getTimeline().size();

        session.addMessage("user", "실패할 턴의 입력");
        session.incrementTurnsSinceLastQuiz();
        session.rollbackUserTurn();

        assertEquals(before, session.getTimeline().size(),
                "롤백 후 타임라인에 실패한 턴의 사용자 메시지가 남으면 안 된다");
        assertEquals("message", session.getTimeline().get(before - 1).get("type"));
    }
}
