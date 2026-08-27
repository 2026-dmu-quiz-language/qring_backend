package com.qring.qring_backend.service.content;

import com.qring.qring_backend.domain.content.StorySession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 턴 프롬프트 조립 로직 검증 (네트워크 호출 없음). */
class OpenAiStoryServiceTest {

    private final OpenAiStoryService service = new OpenAiStoryService();

    private StorySession newSession() {
        return StorySession.builder()
                .sessionId("sess-test")
                .userId(1L)
                .characterName("지민")
                .situationDescription("뉴욕 카페에서 수다 떠는 상황")
                .tone("다정하게")
                .targetLanguage("English")
                .levelCode(1)
                .build();
    }

    @Test
    @DisplayName("오프닝 프롬프트에 세션의 정규화된 값이 그대로 들어간다")
    void openingPromptUsesNormalizedSessionValues() {
        StorySession session = newSession();

        String prompt = service.buildOpeningSystemPrompt(session);

        assertTrue(prompt.contains("AI Partner \"지민\""));
        assertTrue(prompt.contains("Situation: 뉴욕 카페에서 수다 떠는 상황"));
        assertTrue(prompt.contains("Target Language: English (User Level Code: 1)"));
        assertFalse(prompt.contains("null"));
    }

    @Test
    @DisplayName("첫 인사말에 답장한 턴은 퀴즈 채점 지시가 붙지 않는다")
    void firstReplyIsNotGradedAsQuizAnswer() {
        StorySession session = newSession();
        session.addMessage("assistant", "Hey! What are you drinking today?");
        session.addMessage("user", "I'll have an iced americano.");
        session.incrementTurnsSinceLastQuiz();

        String prompt = service.buildTurnSystemPrompt(session, "I'll have an iced americano.");

        assertFalse(prompt.contains("QUIZ ANSWER GRADING"),
                "대기 중인 퀴즈가 없는데 채점 지시문이 들어가면 안 된다");
        assertTrue(prompt.contains("NO quiz is pending"));
        assertTrue(prompt.contains("MUST NOT grade"));
    }

    @Test
    @DisplayName("직전 턴에 퀴즈가 출제된 경우에만 정답/오답 채점 지시가 붙는다")
    void answerAfterQuizIsGradedAgainstCorrectAnswer() {
        StorySession session = newSession();
        session.recordQuiz(Map.of(
                "question", "'디저트'를 뜻하는 표현은?",
                "correct_answer", "dessert",
                "acceptable_answers", List.of("dessert", "sweets")
        ));
        session.incrementTurnsSinceLastQuiz();

        String prompt = service.buildTurnSystemPrompt(session, "dessert");

        assertTrue(prompt.contains("QUIZ ANSWER GRADING"));
        assertTrue(prompt.contains("Correct answer: dessert"));
        assertTrue(prompt.contains("dessert | sweets"));
        assertTrue(prompt.contains("ALREADY graded this answer as CORRECT"),
                "정답 제출 시 서버 판정 결과가 프롬프트에 통보되어야 한다");
    }

    @Test
    @DisplayName("correct_answer 가 없는 주관식 퀴즈도 정답을 (unknown) 으로 알려주지 않는다")
    void subjectiveQuizWithoutCorrectAnswerStillShowsAnAnswer() {
        StorySession session = newSession();
        session.recordQuiz(Map.of(
                "quiz_type", "subjective",
                "question", "'초콜릿 케이크'를 영어로 표현해 보세요.",
                "acceptable_answers", List.of("chocolate cake", "a chocolate cake")));
        session.incrementTurnsSinceLastQuiz();

        String prompt = service.buildTurnSystemPrompt(session, "chocolate cake");

        assertFalse(prompt.contains("Correct answer: (unknown)"),
                "허용 답안이 있는데 정답을 (unknown) 으로 넘기면 모델이 채점을 포기한다");
        assertTrue(prompt.contains("Correct answer: chocolate cake"));
        assertTrue(prompt.contains("chocolate cake | a chocolate cake"));
    }

    @Test
    @DisplayName("퀴즈 채점이 끝나면 다시 채점 지시가 사라진다")
    void gradingDirectiveClearsAfterQuizResolved() {
        StorySession session = newSession();
        session.recordQuiz(Map.of("question", "q", "correct_answer", "dessert"));
        session.clearPendingQuiz();
        session.incrementTurnsSinceLastQuiz();

        assertFalse(service.buildTurnSystemPrompt(session, "Sounds good!")
                .contains("QUIZ ANSWER GRADING"));
    }

    @Test
    @DisplayName("퀴즈 5개를 모두 소진하면 추가 출제를 금지하는 지시가 붙는다")
    void exhaustedQuizBudgetForbidsMoreQuizzes() {
        StorySession session = newSession();
        for (int i = 0; i < OpenAiStoryService.MAX_QUIZ_COUNT; i++) {
            session.recordQuiz(Map.of("question", "q" + i, "correct_answer", "a" + i));
        }
        session.clearPendingQuiz();
        session.incrementTurnsSinceLastQuiz();
        session.incrementTurnsSinceLastQuiz();

        String prompt = service.buildTurnSystemPrompt(session, "Nice!");

        assertTrue(prompt.contains("QUIZ BUDGET EXHAUSTED"));
        assertFalse(prompt.contains("You SHOULD now present a relevant quiz moment"));
    }

    @Test
    @DisplayName("퀴즈 정답이 곧 대화 답변이 되도록 설계하라는 지시가 들어간다")
    void quizMustDoubleAsConversationReply() {
        StorySession session = newSession();
        session.incrementTurnsSinceLastQuiz();
        session.incrementTurnsSinceLastQuiz();

        String prompt = service.buildTurnSystemPrompt(session, "Sounds good!");

        assertTrue(prompt.contains("A QUIZ IS PART OF THE CONVERSATION, NOT A POP-UP TEST"));
        assertTrue(prompt.contains("its correct answer is ALSO a natural, valid reply"));
        assertTrue(prompt.contains("have they also"), "정답이 대화 답변이 되는지 자문하라는 검증 지시가 있어야 한다");
    }

    @Test
    @DisplayName("정답 판정을 앞세우지 말고 대화에 녹이라는 지시가 들어간다")
    void correctAnswerReactionMustStayInCharacter() {
        StorySession session = newSession();
        session.recordQuiz(Map.of("question", "'녹차'를 뜻하는 표현은?", "correct_answer", "green tea"));
        session.incrementTurnsSinceLastQuiz();

        String prompt = service.buildTurnSystemPrompt(session, "green tea");

        assertTrue(prompt.contains("React to WHAT THEY SAID"));
        assertTrue(prompt.contains("DO NOT open with a bare verdict"));
        assertTrue(prompt.contains("DO NOT ask again"));
    }

    @Test
    @DisplayName("오답은 덮지 말고 오답에 맞는 반응을 하라고 지시한다")
    void wrongAnswerGetsItsOwnReaction() {
        StorySession session = newSession();
        session.recordQuiz(Map.of("question", "'녹차'를 뜻하는 표현은?", "correct_answer", "green tea"));
        session.incrementTurnsSinceLastQuiz();

        String prompt = service.buildTurnSystemPrompt(session, "black coffee");

        assertTrue(prompt.contains("React to what they ACTUALLY said"));
        assertTrue(prompt.contains("respond to that"),
                "다른 뜻의 표현이면 그 뜻에 반응하라는 지시가 있어야 한다");
        assertTrue(prompt.contains("NEVER pretend they said the correct expression"));
        assertTrue(prompt.contains("NEVER quietly skip past the mistake"));
        assertTrue(prompt.contains("ALREADY graded this answer as INCORRECT"),
                "오답 제출 시 서버 판정 결과가 프롬프트에 통보되어야 한다");
    }

    @Test
    @DisplayName("서버 채점: 객관식·단어배열은 확정하고, 주관식 목록 밖 답안은 모델에 위임한다")
    void serverGradesDeterministically() {
        Map<String, Object> mc = Map.of("quiz_type", "multiple_choice", "correct_answer", "green tea");
        assertEquals("correct", OpenAiStoryService.gradeAnswer(mc, "  Green Tea. "));
        assertEquals("incorrect", OpenAiStoryService.gradeAnswer(mc, "black coffee"));

        Map<String, Object> wa = Map.of("quiz_type", "word_arrange", "correct_answer", "Let's sit by the window");
        assertEquals("correct", OpenAiStoryService.gradeAnswer(wa, "let's  sit by the window!"));
        assertEquals("incorrect", OpenAiStoryService.gradeAnswer(wa, "window the by sit Let's"),
                "단어 배열은 어순이 틀리면 서버가 오답으로 확정해야 한다");

        Map<String, Object> subj = Map.of("quiz_type", "subjective",
                "acceptable_answers", List.of("nervous", "I'm nervous"));
        assertEquals("correct", OpenAiStoryService.gradeAnswer(subj, "I'm nervous"));
        assertNull(OpenAiStoryService.gradeAnswer(subj, "nervus"),
                "목록 밖 주관식 답안은 모델 위임(null)이어야 한다");
    }

    @Test
    @DisplayName("퀴즈 출제 턴에는 덜 쓰인 유형이 프롬프트에 지정된다")
    void quizTypeIsSteeredTowardsUnusedTypes() {
        assertEquals("multiple_choice", OpenAiStoryService.pickNextQuizType(List.of()));
        assertEquals("word_arrange", OpenAiStoryService.pickNextQuizType(List.of("multiple_choice")));
        assertEquals("subjective", OpenAiStoryService.pickNextQuizType(List.of("multiple_choice", "word_arrange")));
        assertEquals("multiple_choice",
                OpenAiStoryService.pickNextQuizType(List.of("multiple_choice", "word_arrange", "subjective")));

        StorySession session = newSession();
        session.incrementTurnsSinceLastQuiz();
        session.incrementTurnsSinceLastQuiz();

        String prompt = service.buildTurnSystemPrompt(session, "Sounds good!");
        assertTrue(prompt.contains("REQUIRED QUIZ TYPE FOR THIS QUIZ: \"multiple_choice\""));
    }

    @Test
    @DisplayName("말투는 어조가 아니라 관계로 정하라고 지시한다 (다정하게 != 반말 강제)")
    void speechLevelFollowsRelationshipNotToneWord() {
        StorySession session = newSession();
        session.incrementTurnsSinceLastQuiz();

        for (String prompt : List.of(
                service.buildOpeningSystemPrompt(session),
                service.buildTurnSystemPrompt(session, "Hi"))) {

            assertTrue(prompt.contains("It does NOT by itself decide the\n              politeness level.")
                            || prompt.contains("does NOT by itself decide"),
                    "어조가 높임법을 결정하지 않는다는 지시가 있어야 한다");
            assertTrue(prompt.contains("If the relationship is genuinely unclear, default to 존댓말."));
            assertTrue(prompt.contains("It does NOT mean dropping honorifics."));

            // 예전의 무조건적 반말 강제 문구가 남아 있으면 안 된다
            assertFalse(prompt.contains("MUST STRICTLY USE FRIENDLY CASUAL BANMAL"));
            assertFalse(prompt.contains("NEVER use formal honorifics"));
        }
    }

    @Test
    @DisplayName("사용자 입력에 % 가 있어도 프롬프트 조립이 깨지지 않는다")
    void percentSignInUserInputIsSafe() {
        StorySession session = newSession();
        session.setSituationDescription("50% 할인 행사 중인 카페");
        session.incrementTurnsSinceLastQuiz();

        String prompt = service.buildTurnSystemPrompt(session, "It's 100%% off? %s wow");

        assertTrue(prompt.contains("It's 100%% off? %s wow"));
    }
}
