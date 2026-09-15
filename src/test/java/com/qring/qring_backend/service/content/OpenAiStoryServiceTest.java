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
        assertTrue(prompt.contains("Their correct answer IS their reply"));
        assertTrue(prompt.contains("have they answered my question in the scene"),
                "정답이 대화 답변이 되는지 자문하라는 검증 지시가 있어야 한다");
        assertTrue(prompt.contains("MUST therefore be a CLOSED question"),
                "답이 정해지지 않은 열린 질문을 금지해야 한다");
        assertTrue(prompt.contains("quizzing a word that appears in your own question"),
                "자기 질문 속 단어를 정답으로 내는 앵무새 퀴즈를 금지해야 한다");
        assertTrue(prompt.contains("SHORT ANSWER ONLY"), "주관식은 단답만 내야 한다");
        assertTrue(prompt.contains("NEVER ask the learner to type a full sentence"));
        assertTrue(prompt.contains("MUST end with the ONE closed in-story question"),
                "퀴즈 턴 페이싱 지시에 질문으로 끝내라는 요구가 있어야 한다");
        assertTrue(prompt.contains("OUTPUT LANGUAGE (ABSOLUTE)"));
        assertFalse(prompt.contains("녹차 좋지. 따뜻한 걸로 줄까?"), "베낄 수 있는 한국어 예시 문장은 없어야 한다");
        assertFalse(prompt.contains("그건 블랙커피잖아"), "베낄 수 있는 한국어 예시 문장은 없어야 한다");
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

        assertTrue(prompt.contains("react briefly to that"),
                "다른 뜻의 표현이면 그 뜻에 반응하라는 지시가 있어야 한다");
        assertTrue(prompt.contains("word-order or missing-word mistake"),
                "단어 배열 오답은 빠진 단어/어순으로 반응하라는 별도 지시가 있어야 한다");
        assertTrue(prompt.contains("quote the \"Correct answer\" above EXACTLY"),
                "정답을 글자 그대로 인용하라는 지시가 있어야 한다");
        assertTrue(prompt.contains("NEVER pretend they said the correct expression"));
        assertTrue(prompt.contains("NEVER quietly skip past the mistake"));
        assertTrue(prompt.contains("ALREADY graded this answer as INCORRECT"),
                "오답 제출 시 서버 판정 결과가 프롬프트에 통보되어야 한다");
        assertTrue(prompt.contains("wrong attempt 1 of 3"));
        assertFalse(prompt.contains("LAST allowed attempt"));
    }

    @Test
    @DisplayName("3번째 오답 턴에는 정답을 공개하고 넘어가라고 지시한다")
    void lastWrongAttemptRevealsAnswer() {
        StorySession session = newSession();
        session.recordQuiz(Map.of("question", "'녹차'를 뜻하는 표현은?", "correct_answer", "green tea"));
        session.recordWrongAttempt();
        session.recordWrongAttempt();
        session.incrementTurnsSinceLastQuiz();

        String prompt = service.buildTurnSystemPrompt(session, "black coffee");

        assertTrue(prompt.contains("wrong attempt 3 of 3"));
        assertTrue(prompt.contains("LAST allowed attempt"));
        assertTrue(prompt.contains("Do NOT invite another try"));
    }

    @Test
    @DisplayName("객관식에서 보기 밖 딴 얘기를 하면 채점하지 말고 질문으로 되돌리라고 지시한다")
    void offTopicInputIsNotGraded() {
        StorySession session = newSession();
        session.recordQuiz(Map.of("quiz_type", "multiple_choice", "question", "'녹차'를 뜻하는 표현은?",
                "correct_answer", "green tea", "options", List.of("green tea", "black coffee", "milk")));
        session.incrementTurnsSinceLastQuiz();

        String prompt = service.buildTurnSystemPrompt(session, "바운디라는 가순데 알아?");

        assertTrue(prompt.contains("did NOT attempt the quiz"));
        assertTrue(prompt.contains("steer them back to your pending in-story question"));
        assertFalse(prompt.contains("ALREADY graded this answer as INCORRECT"));
        assertTrue(prompt.contains("A quiz is still pending"), "대기 중에는 새 퀴즈를 만들지 말라는 페이싱 지시가 있어야 한다");
        assertFalse(prompt.contains("REQUIRED QUIZ TYPE FOR THIS QUIZ"));
    }

    @Test
    @DisplayName("서버 답안 분류: 정답 / 오답 / 시도 아님 / 모델 위임")
    void classifiesAnswers() {
        Map<String, Object> mc = Map.of("quiz_type", "multiple_choice", "correct_answer", "green tea",
                "options", List.of("green tea", "black coffee", "milk"));
        assertEquals("correct", OpenAiStoryService.classifyAnswer(mc, "Green tea"));
        assertEquals("incorrect", OpenAiStoryService.classifyAnswer(mc, "black coffee"));
        assertEquals(OpenAiStoryService.NOT_ATTEMPT, OpenAiStoryService.classifyAnswer(mc, "바운디라는 가순데 알아?"));

        Map<String, Object> wa = Map.of("quiz_type", "word_arrange", "correct_answer", "I want to play mid",
                "tiles", List.of("play", "I", "mid", "to", "want"));
        assertEquals("correct", OpenAiStoryService.classifyAnswer(wa, "I want to play mid"));
        assertEquals("incorrect", OpenAiStoryService.classifyAnswer(wa, "I want play mid"),
                "타일 단어만으로 만든 다른 문장은 오답(시도)이다");
        assertEquals(OpenAiStoryService.NOT_ATTEMPT, OpenAiStoryService.classifyAnswer(wa, "롤 알아?"),
                "타일에 없는 단어를 쓴 입력은 시도가 아니다");

        Map<String, Object> subj = Map.of("quiz_type", "subjective", "acceptable_answers", List.of("often"));
        assertEquals("correct", OpenAiStoryService.classifyAnswer(subj, "Often!"));
        assertNull(OpenAiStoryService.classifyAnswer(subj, "ofen"), "주관식 목록 밖 입력은 모델 위임");
        assertNull(OpenAiStoryService.classifyAnswer(subj, "게임 얘기 할래?"), "주관식은 시도 여부도 모델이 가린다");
    }

    @Test
    @DisplayName("퀴즈 정리: 단어배열 타일은 정답 단어와 정확히 같아야 하고, 객관식 보기에는 정답이 있어야 한다")
    void sanitizesQuizObjects() {
        Map<String, Object> badTiles = Map.of("quiz_type", "word_arrange",
                "correct_answer", "I want to play mid role",
                "tiles", List.of("I", "want", "play", "mid", "role")); // 'to' 누락 (실측 사례)
        Map<String, Object> fixed = OpenAiStoryService.sanitizeQuiz(badTiles);
        @SuppressWarnings("unchecked")
        List<String> tiles = (List<String>) fixed.get("tiles");
        assertEquals(List.of("I", "mid", "play", "role", "to", "want"),
                tiles.stream().sorted().toList(), "타일은 정답의 단어 집합과 같아야 한다");
        assertEquals("I want to play mid role", fixed.get("correct_answer"));

        Map<String, Object> goodTiles = Map.of("quiz_type", "word_arrange",
                "correct_answer", "Let's sit by the window",
                "tiles", List.of("window", "the", "by", "sit", "Let's"));
        assertEquals(List.of("window", "the", "by", "sit", "Let's"),
                OpenAiStoryService.sanitizeQuiz(goodTiles).get("tiles"), "이미 맞는 타일은 건드리지 않는다");

        Map<String, Object> mcMissing = Map.of("quiz_type", "multiple_choice", "correct_answer", "iced",
                "options", List.of("hot", "boiled", "grilled"));
        @SuppressWarnings("unchecked")
        List<String> options = (List<String>) OpenAiStoryService.sanitizeQuiz(mcMissing).get("options");
        assertTrue(options.contains("iced"), "정답이 보기에 없으면 보기에 넣는다");
        assertEquals(4, options.size());

        Map<String, Object> subjNoCorrect = Map.of("quiz_type", "subjective", "acceptable_answers", List.of("often", "a lot"));
        assertEquals("often", OpenAiStoryService.sanitizeQuiz(subjNoCorrect).get("correct_answer"));
    }

    @Test
    @DisplayName("정답이 AI 질문 안에 그대로 들어 있는 앵무새 퀴즈를 판별한다")
    void detectsEchoedAnswer() {
        Map<String, Object> echo = Map.of("quiz_type", "multiple_choice", "correct_answer", "favorite song");
        assertTrue(OpenAiStoryService.isAnswerEchoedInMessage("Do you have a favorite song of his?", echo));

        Map<String, Object> fine = Map.of("quiz_type", "subjective", "acceptable_answers", List.of("often"));
        assertFalse(OpenAiStoryService.isAnswerEchoedInMessage("Do you play a lot?", fine));

        Map<String, Object> choice = Map.of("quiz_type", "subjective", "acceptable_answers", List.of("iced"));
        assertFalse(OpenAiStoryService.isAnswerEchoedInMessage("Would you like it hot or iced?", choice),
                "선택형 질문의 한 단어 답은 질문에 나와도 정상이다");

        Map<String, Object> lifted = Map.of("quiz_type", "subjective", "acceptable_answers", List.of("favorite"));
        assertTrue(OpenAiStoryService.isAnswerEchoedInMessage("Do you have a favorite song by them?", lifted),
                "선택형이 아닌 질문에서 들어 낸 한 단어는 앵무새 퀴즈다");

        Map<String, Object> phraseChoice = Map.of("quiz_type", "multiple_choice", "correct_answer", "with friends");
        assertFalse(OpenAiStoryService.isAnswerEchoedInMessage("Do you usually go alone or with friends?", phraseChoice),
                "선택형 질문의 짧은 구절 선택지도 정상이다");

        Map<String, Object> partial = Map.of("quiz_type", "multiple_choice", "correct_answer", "song");
        assertFalse(OpenAiStoryService.isAnswerEchoedInMessage("Any songs you like?", partial),
                "단어 경계로 비교하므로 'song'은 'songs'에 걸리지 않는다");

        Map<String, Object> arrange = Map.of("quiz_type", "word_arrange", "correct_answer", "I play a lot");
        assertFalse(OpenAiStoryService.isAnswerEchoedInMessage("I play a lot too", arrange), "단어 배열은 검사 대상이 아니다");
    }

    @Test
    @DisplayName("퀴즈의 'asked'(방금 한 질문)가 실제 AI 대사에 있는지 검사한다")
    void checksQuizIsLinkedToMessage() {
        String aiMsg = "Sounds fun! Do you play a lot?";
        assertTrue(OpenAiStoryService.isQuizLinkedToMessage(aiMsg,
                Map.of("asked", "Do you play a lot?", "correct_answer", "often")));
        assertTrue(OpenAiStoryService.isQuizLinkedToMessage(aiMsg,
                Map.of("correct_answer", "often")), "asked 필드가 없으면 검사하지 않는다");
        assertFalse(OpenAiStoryService.isQuizLinkedToMessage(aiMsg,
                Map.of("asked", "Do you want it hot or iced?", "correct_answer", "iced")),
                "대사에 없는 질문을 적은 퀴즈(예시 베끼기)는 걸러야 한다");
    }

    @Test
    @DisplayName("응답 본문의 공백·코드 펜스·잡문을 걷어내고 JSON 객체를 파싱한다")
    void parsesJsonObjectRobustly() {
        assertEquals("hi", service.parseJsonObject("\n {\"ai_message\": \"hi\"} \n").get("ai_message"));
        assertEquals("hi", service.parseJsonObject("```json\n{\"ai_message\": \"hi\"}\n```").get("ai_message"));
        assertEquals("hi", service.parseJsonObject("Sure! {\"ai_message\": \"hi\"} Done.").get("ai_message"));
        assertNull(service.parseJsonObject("Oh, I know Boundy! Which song do you like?"));
        assertNull(service.parseJsonObject("{not json}"));
    }

    @Test
    @DisplayName("대상 언어가 한국어가 아닌데 AI 대사에 한글이 섞이면 감지한다")
    void detectsHangulInTargetLanguageMessage() {
        assertTrue(OpenAiStoryService.hasUnexpectedHangul("English", "Coffee it is! 따뜻한 커피 좋지~"));
        assertFalse(OpenAiStoryService.hasUnexpectedHangul("English", "Coffee it is! Want it hot?"));
        assertFalse(OpenAiStoryService.hasUnexpectedHangul("Korean", "커피 좋지!"));
    }

    @Test
    @DisplayName("마지막 퀴즈를 채점하는 턴에는 스토리 마무리 지시가 붙는다")
    void finalQuizGradingTurnGetsClosingDirective() {
        StorySession session = newSession();
        for (int i = 0; i < OpenAiStoryService.MAX_QUIZ_COUNT - 1; i++) {
            session.recordQuiz(Map.of("question", "q" + i, "correct_answer", "a" + i));
            session.clearPendingQuiz();
        }
        session.recordQuiz(Map.of("question", "'녹차'를 뜻하는 표현은?", "correct_answer", "green tea"));
        session.incrementTurnsSinceLastQuiz();

        String prompt = service.buildTurnSystemPrompt(session, "green tea");

        assertTrue(prompt.contains("THIS IS THE FINAL QUIZ OF THE SESSION"));
        assertTrue(prompt.contains("bring the situation to a warm, natural conclusion"));
        assertTrue(prompt.contains("do not close the story yet"),
                "오답 재시도 시에는 마무리하지 말라는 예외가 있어야 한다");
        assertFalse(prompt.contains("QUIZ BUDGET EXHAUSTED"),
                "마지막 퀴즈 채점 중에는 채점 지시가 우선이므로 소진 지시가 겹치면 안 된다");
    }

    @Test
    @DisplayName("이어하기로 한도가 늘면 5번째 퀴즈 채점도 마지막이 아니게 되고 프롬프트에 새 한도가 반영된다")
    void extendedLimitIsReflectedInPrompt() {
        StorySession session = newSession();
        for (int i = 0; i < StorySession.DEFAULT_QUIZ_LIMIT; i++) {
            session.recordQuiz(Map.of("question", "q" + i, "correct_answer", "a" + i));
            session.clearPendingQuiz();
        }
        session.extendQuizLimit(5); // 한도 10
        session.incrementTurnsSinceLastQuiz();
        session.incrementTurnsSinceLastQuiz();

        String prompt = service.buildTurnSystemPrompt(session, "Sounds good!");

        assertTrue(prompt.contains("Current Quiz Count Given So Far: 5 / 10"));
        assertFalse(prompt.contains("QUIZ BUDGET EXHAUSTED"), "한도가 늘었으니 소진 지시가 나오면 안 된다");
        assertTrue(prompt.contains("REQUIRED QUIZ TYPE"), "연장 후에는 다시 퀴즈 출제가 허용되어야 한다");
        assertTrue(prompt.contains("STORY PROGRESS NOTE"),
                "연장된 세션은 오프닝 Situation 이 아니라 현재 장면을 따르라는 지시가 있어야 한다");
    }

    @Test
    @DisplayName("연장하지 않은 세션의 턴 프롬프트에는 장면 진행 지시가 없다")
    void unextendedSessionHasNoStoryProgressNote() {
        String prompt = service.buildTurnSystemPrompt(newSession(), "Hi!");

        assertFalse(prompt.contains("STORY PROGRESS NOTE"));
    }

    @Test
    @DisplayName("이어하기 프롬프트는 다음 장면으로 넘어가되 초기화·퀴즈 언급을 금지한다")
    void continuationPromptMovesToNextScene() {
        String prompt = service.buildContinuationSystemPrompt(newSession());

        assertTrue(prompt.contains("THE LEARNER CHOSE TO CONTINUE THE STORY"));
        assertTrue(prompt.contains("MOVE THE STORY TO A NEW SCENE"));
        assertTrue(prompt.contains("Do NOT stay in the finished scene"));
        assertTrue(prompt.contains("Do NOT restart the story"));
        assertTrue(prompt.contains("do NOT mention quizzes"));
        assertTrue(prompt.contains("ONE closed question"));
        assertTrue(prompt.contains("NEVER narrate like a novel"), "2인칭 해설 지문을 금지해야 한다");
        assertTrue(prompt.contains("Only the two of you are in this story"));
        assertTrue(prompt.contains("OUTPUT LANGUAGE (ABSOLUTE)"));
    }

    @Test
    @DisplayName("비퀴즈 턴 지시는 매 턴 질문으로 끝내라고 강제하지 않는다")
    void nonQuizTurnDoesNotForceQuestions() {
        StorySession session = newSession();
        session.incrementTurnsSinceLastQuiz(); // 1턴 → 아직 퀴즈 불가

        String prompt = service.buildTurnSystemPrompt(session, "I'll have a latte.");

        assertTrue(prompt.contains("Do NOT end every message with a question"));
        assertTrue(prompt.contains("A question is optional, not required"));
        assertFalse(prompt.contains("ask an engaging follow-up question"));
        assertFalse(prompt.contains("REJECTED by the server"), "위협 문구는 제거되어야 한다");
    }

    @Test
    @DisplayName("마지막 퀴즈 마무리는 현재 장면만 가볍게 닫고 영영 작별하지 않는다")
    void finalQuizClosingIsLight() {
        StorySession session = newSession();
        for (int i = 0; i < StorySession.DEFAULT_QUIZ_LIMIT; i++) {
            session.recordQuiz(Map.of("question", "q" + i, "correct_answer", "a" + i));
        }
        session.incrementTurnsSinceLastQuiz();

        String prompt = service.buildTurnSystemPrompt(session, "a4");

        assertTrue(prompt.contains("THIS IS THE FINAL QUIZ OF THE SESSION"));
        assertTrue(prompt.contains("close the CURRENT SCENE"));
        assertTrue(prompt.contains("do not part ways for good"));
        assertFalse(prompt.contains("say goodbye and part"));
    }

    @Test
    @DisplayName("마지막 퀴즈가 아닌 채점 턴에는 마무리 지시가 없다")
    void nonFinalGradingTurnHasNoClosingDirective() {
        StorySession session = newSession();
        session.recordQuiz(Map.of("question", "'녹차'를 뜻하는 표현은?", "correct_answer", "green tea"));
        session.incrementTurnsSinceLastQuiz();

        assertFalse(service.buildTurnSystemPrompt(session, "green tea")
                .contains("THIS IS THE FINAL QUIZ OF THE SESSION"));
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
