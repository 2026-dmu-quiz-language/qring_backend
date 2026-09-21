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
        assertTrue(prompt.contains("Do NOT re-check their spelling, spacing or word order"),
                "이미 정답인데 어순을 지적하던 실측 사고의 재발 방지");
        assertTrue(prompt.contains("READ IT FROM THEIR SIDE"),
                "학습자의 대사를 AI 쪽에서 읽어 뜻을 뒤집던 실측 사고의 재발 방지");
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
        assertTrue(prompt.contains("have they actually replied to your question in the scene?"),
                "정답이 대화 답변이 되는지 자문하라는 검증 지시가 있어야 한다");
        assertTrue(prompt.contains("HOW TO BUILD A QUIZ - follow these steps IN THIS ORDER"),
                "예시 대신 퀴즈 제작 절차가 단계로 들어가야 한다");
        assertTrue(prompt.contains("CLOSED: the set of sensible answers is small"),
                "답이 정해지지 않은 열린 질문을 금지해야 한다");
        assertTrue(prompt.contains("Is the answer absent from your own question?"),
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

        assertTrue(prompt.contains("NEVER explain the word order"), "어순 설명 금지");
        assertTrue(prompt.contains("react briefly to that"),
                "다른 뜻의 표현이면 그 뜻에 반응하라는 지시가 있어야 한다");
        assertTrue(prompt.contains("word-order or missing-word mistake"),
                "단어 배열 오답은 빠진 단어/어순으로 반응하라는 별도 지시가 있어야 한다");
        assertTrue(prompt.contains("DO NOT REVEAL THE CORRECT ANSWER YET"),
                "1~2회째 오답에는 정답을 공개하지 말고 힌트만 주라는 지시가 있어야 한다");
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

        // 한국어 입력은 이제 오답으로 확정되므로(2026-09-22), 미시도는 대상 언어로 딴 얘기를 한 경우로 본다
        String prompt = service.buildTurnSystemPrompt(session, "Do you know that singer?");

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
        assertEquals(OpenAiStoryService.NOT_ATTEMPT, OpenAiStoryService.classifyAnswer(mc, "Do you know that singer?"));
        assertEquals("incorrect", OpenAiStoryService.classifyAnswer(mc, "바운디라는 가순데 알아?"),
                "대상 언어로 답해야 하는 자리에 한국어로 답하면 오답 (팀 결정 2026-09-22)");

        Map<String, Object> wa = Map.of("quiz_type", "word_arrange", "correct_answer", "I want to play mid",
                "tiles", List.of("play", "I", "mid", "to", "want"));
        assertEquals("correct", OpenAiStoryService.classifyAnswer(wa, "I want to play mid"));
        assertEquals("incorrect", OpenAiStoryService.classifyAnswer(wa, "I want play mid"),
                "타일 단어만으로 만든 다른 문장은 오답(시도)이다");
        assertEquals(OpenAiStoryService.NOT_ATTEMPT, OpenAiStoryService.classifyAnswer(wa, "do you know that game"),
                "타일에 없는 단어를 쓴 입력은 시도가 아니다");
        assertEquals("incorrect", OpenAiStoryService.classifyAnswer(wa, "롤 알아?"),
                "한국어 입력은 오답으로 확정한다 (2026-09-22)");

        Map<String, Object> subj = Map.of("quiz_type", "subjective", "acceptable_answers", List.of("often"));
        assertEquals("correct", OpenAiStoryService.classifyAnswer(subj, "Often!"));
        assertNull(OpenAiStoryService.classifyAnswer(subj, "ofen"), "주관식 목록 밖 입력은 모델 위임");
        assertEquals("incorrect", OpenAiStoryService.classifyAnswer(subj, "게임 얘기 할래?"),
                "주관식도 한국어 입력은 오답으로 확정한다 (2026-09-22)");
        assertNull(OpenAiStoryService.classifyAnswer(subj, "do you want to talk about games"),
                "대상 언어로 한 목록 밖 입력은 모델이 가린다");
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

        Map<String, Object> punctuated = Map.of("quiz_type", "word_arrange",
                "correct_answer", "I want ketchup, please!",
                "tiles", List.of("please!", "I", "ketchup,", "want"));
        Map<String, Object> cleaned = OpenAiStoryService.sanitizeQuiz(punctuated);
        assertEquals("I want ketchup, please", cleaned.get("correct_answer"), "정답 끝 문장부호는 뗀다");
        @SuppressWarnings("unchecked")
        List<String> cleanedTiles = (List<String>) cleaned.get("tiles");
        assertEquals(List.of("I", "ketchup,", "please", "want"), cleanedTiles.stream().sorted().toList());

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

        Map<String, Object> arrange = Map.of("quiz_type", "word_arrange", "correct_answer", "I play often");
        assertFalse(OpenAiStoryService.isAnswerEchoedInMessage("I play often too", arrange), "3단어 이하 단어 배열은 검사하지 않는다");

        Map<String, Object> ownQuestion = Map.of("quiz_type", "word_arrange",
                "correct_answer", "Do you want to eat something salty or sweet");
        assertTrue(OpenAiStoryService.isAnswerEchoedInMessage("Oh, you're hungry? Do you want to eat something salty or sweet?", ownQuestion),
                "AI 자기 질문 문장을 배열시키는 퀴즈는 거부한다");
        Map<String, Object> nearOwnQuestion = Map.of("quiz_type", "word_arrange",
                "correct_answer", "What did the boss tell Selena to do");
        assertTrue(OpenAiStoryService.isAnswerEchoedInMessage("You brought up Selena? What exactly did the boss tell Selena to do?", nearOwnQuestion),
                "단어 하나가 끼어 있어도 80% 이상 겹치면 자기 질문 배열이다");
        Map<String, Object> realReply = Map.of("quiz_type", "word_arrange", "correct_answer", "Because of the boss's lies");
        assertFalse(OpenAiStoryService.isAnswerEchoedInMessage("So tell me, why don't you trust me anymore?", realReply));
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
    @DisplayName("오프닝 번역의 말투를 판별한다 (반말/존댓말/판별 불가)")
    void detectsSpeechLevel() {
        assertEquals("반말", OpenAiStoryService.detectSpeechLevel("야! 여기 진짜 멋지지 않아? 너는 오늘 뭐 시킬 거야?"));
        assertEquals("존댓말", OpenAiStoryService.detectSpeechLevel("어서 오세요! 무엇을 주문하시겠어요? 창가 자리로 안내할까요?"));
        assertEquals("반말", OpenAiStoryService.detectSpeechLevel("안녕! 만나서 반가워. 오늘 날씨 좋죠?"), "다수결");
        assertNull(OpenAiStoryService.detectSpeechLevel(""));
        assertNull(OpenAiStoryService.detectSpeechLevel("Hello there!"));
    }

    @Test
    @DisplayName("말투가 고정된 세션의 프롬프트에는 고정 지시가 들어간다")
    void speechLevelLockAppearsInPrompts() {
        StorySession session = newSession();
        assertFalse(service.buildTurnSystemPrompt(session, "hi").contains("SPEECH LEVEL LOCKED"));

        session.setSpeechLevel("반말");
        assertTrue(service.buildTurnSystemPrompt(session, "hi").contains("SPEECH LEVEL LOCKED FOR THIS SESSION: 반말"));
        assertTrue(service.buildContinuationSystemPrompt(session).contains("SPEECH LEVEL LOCKED FOR THIS SESSION: 반말"));
    }

    @Test
    @DisplayName("문장형 주관식은 단어배열로 자동 변환된다")
    void longSubjectiveBecomesWordArrange() {
        Map<String, Object> longSubjective = Map.of("quiz_type", "subjective",
                "question", "'나는 보통 외식하는 편이야'를 영어로 어떻게 말해?",
                "reply_meaning", "나는 보통 외식하는 편이야",
                "acceptable_answers", List.of("I usually eat out"), "hint", "eat이 들어가요");
        Map<String, Object> fixed = OpenAiStoryService.sanitizeQuiz(longSubjective);

        assertEquals("word_arrange", fixed.get("quiz_type"));
        assertEquals("I usually eat out", fixed.get("correct_answer"));
        @SuppressWarnings("unchecked")
        List<String> tiles = (List<String>) fixed.get("tiles");
        assertEquals(List.of("I", "eat", "out", "usually"), tiles.stream().sorted().toList());
        assertEquals("'나는 보통 외식하는 편이야'가 되도록 단어를 배열해 보세요.", fixed.get("question"));
        assertNull(fixed.get("acceptable_answers"));

        Map<String, Object> shortSubjective = Map.of("quiz_type", "subjective", "acceptable_answers", List.of("often"));
        assertEquals("subjective", OpenAiStoryService.sanitizeQuiz(shortSubjective).get("quiz_type"));
    }

    private StorySession japaneseSession() {
        return StorySession.builder()
                .sessionId("sess-ja").userId(1L).characterName("ゆい")
                .situationDescription("東京のカフェで注文する状況").tone("다정하게")
                .targetLanguage("Japanese").levelCode(1)
                .build();
    }

    @Test
    @DisplayName("일본어·중국어 세션의 프롬프트에만 띄어쓰기 없는 언어용 블록이 들어간다")
    void noSpaceScriptDirectiveOnlyForNoSpaceLanguages() {
        String japanese = service.buildOpeningSystemPrompt(japaneseSession());
        assertTrue(japanese.contains("Target Language: Japanese (User Level Code: 1)"));
        assertTrue(japanese.contains("SCRIPT RULES FOR Japanese"));
        assertTrue(japanese.contains("at most " + OpenAiStoryService.MAX_SUBJECTIVE_CHARS + " characters"));

        assertFalse(service.buildOpeningSystemPrompt(newSession()).contains("SCRIPT RULES FOR"));
        assertFalse(service.buildTurnSystemPrompt(newSession(), "hi").contains("SCRIPT RULES FOR"));

        StorySession chinese = japaneseSession();
        chinese.setTargetLanguage("Chinese (Simplified)");
        assertTrue(service.buildTurnSystemPrompt(chinese, "hi").contains("SCRIPT RULES FOR Chinese (Simplified)"));
    }

    @Test
    @DisplayName("일본어 단어배열: 모델 타일을 그대로 두고 정답은 공백·끝 문장부호 없이 정리한다")
    void japaneseWordArrangeKeepsModelTiles() {
        Map<String, Object> quiz = Map.of("quiz_type", "word_arrange",
                "correct_answer", "私は コーヒーが 好きです。",
                "tiles", List.of("好きです。", "私は", "コーヒーが"));
        Map<String, Object> fixed = OpenAiStoryService.sanitizeQuiz(quiz, "Japanese");

        assertEquals("私はコーヒーが好きです", fixed.get("correct_answer"));
        assertEquals(List.of("好きです", "私は", "コーヒーが"), fixed.get("tiles"), "타일은 순서·내용 그대로, 끝 문장부호만 뗀다");
        assertNull(OpenAiStoryService.structuralProblem(fixed, "Japanese"));

        Map<String, Object> noAnswer = Map.of("quiz_type", "word_arrange",
                "tiles", List.of("私は", "コーヒーが", "好きです"));
        assertEquals("私はコーヒーが好きです", OpenAiStoryService.sanitizeQuiz(noAnswer, "Japanese").get("correct_answer"),
                "정답이 없으면 타일을 공백 없이 이어 붙인다");

        // 영어는 기존 규칙 그대로: 정답 단어로 타일을 다시 만든다
        Map<String, Object> english = Map.of("quiz_type", "word_arrange",
                "correct_answer", "I like coffee", "tiles", List.of("I", "coffee"));
        @SuppressWarnings("unchecked")
        List<String> englishTiles = (List<String>) OpenAiStoryService.sanitizeQuiz(english, "English").get("tiles");
        assertEquals(List.of("I", "coffee", "like"), englishTiles.stream().sorted().toList());
    }

    @Test
    @DisplayName("일본어 구조 문제: 타일이 정답을 못 이루거나 하나뿐이면, 주관식이 10자를 넘으면 거부 사유를 돌려준다")
    void japaneseStructuralProblems() {
        Map<String, Object> missingTile = Map.of("quiz_type", "word_arrange",
                "correct_answer", "私はコーヒーが好きです", "tiles", List.of("私は", "好きです"));
        String reason = OpenAiStoryService.structuralProblem(missingTile, "Japanese");
        assertTrue(reason != null && reason.contains("do not join up"), reason);

        Map<String, Object> singleTile = Map.of("quiz_type", "word_arrange",
                "correct_answer", "私はコーヒーが好きです", "tiles", List.of("私はコーヒーが好きです"));
        String single = OpenAiStoryService.structuralProblem(singleTile, "Japanese");
        assertTrue(single != null && single.contains("single tile"), single);

        Map<String, Object> noTiles = Map.of("quiz_type", "word_arrange", "correct_answer", "私はコーヒーが好きです");
        assertTrue(OpenAiStoryService.structuralProblem(noTiles, "Japanese") != null, "타일이 없으면 앱이 낼 수 없는 퀴즈다");

        Map<String, Object> longSubjective = Map.of("quiz_type", "subjective",
                "correct_answer", "毎晩ゲームをしているよ", "acceptable_answers", List.of("毎晩ゲームをしているよ"));
        String tooLong = OpenAiStoryService.structuralProblem(longSubjective, "Japanese");
        assertTrue(tooLong != null && tooLong.contains("at most " + OpenAiStoryService.MAX_SUBJECTIVE_CHARS), tooLong);

        Map<String, Object> shortSubjective = Map.of("quiz_type", "subjective",
                "correct_answer", "よく行くよ", "acceptable_answers", List.of("よく行くよ"));
        assertNull(OpenAiStoryService.structuralProblem(shortSubjective, "Japanese"));

        // 띄어쓰기를 쓰는 언어는 sanitizeQuiz 가 이미 고치므로 구조 검사가 없다
        assertNull(OpenAiStoryService.structuralProblem(noTiles, "English"));
    }

    @Test
    @DisplayName("일본어 채점: 공백·끝 문장부호를 무시하고, 단어배열은 타일 조립 여부로 시도/시도 아님을 가른다")
    void japaneseClassifyAnswer() {
        Map<String, Object> arrange = Map.of("quiz_type", "word_arrange",
                "correct_answer", "私はコーヒーが好きです", "tiles", List.of("好きです", "私は", "コーヒーが"));
        assertEquals("correct", OpenAiStoryService.classifyAnswer(arrange, "私は コーヒーが 好きです", "Japanese"),
                "앱이 타일을 공백으로 이어 보내도 정답이다");
        assertEquals("correct", OpenAiStoryService.classifyAnswer(arrange, "私はコーヒーが好きです。", "Japanese"));
        assertNull(OpenAiStoryService.classifyAnswer(arrange, "コーヒーが私は好きです", "Japanese"),
                "타일을 모두 쓴 다른 순서는 자연스러울 수 있어 모델이 판정한다 (2026-09-22)");
        assertEquals(OpenAiStoryService.NOT_ATTEMPT, OpenAiStoryService.classifyAnswer(arrange, "紅茶が好きです", "Japanese"),
                "타일에 없는 글자가 섞이면 시도가 아니다");
        assertEquals(OpenAiStoryService.NOT_ATTEMPT, OpenAiStoryService.classifyAnswer(arrange, "   ", "Japanese"));

        Map<String, Object> choice = Map.of("quiz_type", "multiple_choice",
                "correct_answer", "冷たいの", "options", List.of("冷たいの", "温かいの"));
        assertEquals("correct", OpenAiStoryService.classifyAnswer(choice, "冷たいの！", "Japanese"));
        assertEquals("incorrect", OpenAiStoryService.classifyAnswer(choice, "温かいの", "Japanese"));
        assertEquals(OpenAiStoryService.NOT_ATTEMPT, OpenAiStoryService.classifyAnswer(choice, "うーん", "Japanese"));

        Map<String, Object> subjective = Map.of("quiz_type", "subjective", "acceptable_answers", List.of("よく"));
        assertEquals("correct", OpenAiStoryService.classifyAnswer(subjective, "よく", "Japanese"));
        assertNull(OpenAiStoryService.classifyAnswer(subjective, "たまに", "Japanese"), "주관식 목록 밖 답은 모델에 위임");
    }

    @Test
    @DisplayName("일본어 대사 연결·앵무새·중복 질문 검사는 문자열 포함과 문자 2-gram 겹침으로 본다")
    void japaneseMessageChecks() {
        String aiMsg = "いいね！コーヒーは好き？";
        assertTrue(OpenAiStoryService.isQuizLinkedToMessage(aiMsg, Map.of("asked", "コーヒーは好き？"), "Japanese"));
        assertTrue(OpenAiStoryService.isQuizLinkedToMessage(aiMsg, Map.of("asked", "コーヒーは好きなの？"), "Japanese"),
                "어미가 조금 달라도 2-gram 이 70% 이상 겹치면 같은 질문이다");
        assertFalse(OpenAiStoryService.isQuizLinkedToMessage(aiMsg, Map.of("asked", "紅茶にする？"), "Japanese"));

        Map<String, Object> parrot = Map.of("quiz_type", "subjective", "acceptable_answers", List.of("よく"));
        assertFalse(OpenAiStoryService.isAnswerEchoedInMessage("ゲームはよくするの？", parrot, "Japanese"),
                "짧은 낱말은 질문에 나오는 것이 자연스러워 통과시킨다 (팀 결정 2026-09-22)");
        Map<String, Object> longParrot = Map.of("quiz_type", "subjective",
                "acceptable_answers", List.of("よく散歩させるよ"));
        assertTrue(OpenAiStoryService.isAnswerEchoedInMessage("君はよく散歩させるよね？", longParrot, "Japanese"),
                "긴 표현을 그대로 베끼게 하는 퀴즈는 여전히 거부한다");
        Map<String, Object> choice = Map.of("quiz_type", "multiple_choice", "correct_answer", "冷たいのがいい");
        assertFalse(OpenAiStoryService.isAnswerEchoedInMessage("温かいの？それとも冷たいのがいい？", choice, "Japanese"),
                "선택형 질문(それとも)의 선택지는 질문에 나와도 정상이다");
        assertTrue(OpenAiStoryService.isAnswerEchoedInMessage("冷たいのがいいよね？", choice, "Japanese"));
        Map<String, Object> ownSentence = Map.of("quiz_type", "word_arrange",
                "correct_answer", "私はコーヒーが好きです", "tiles", List.of("私は", "コーヒーが", "好きです"));
        assertTrue(OpenAiStoryService.isAnswerEchoedInMessage("私はコーヒーが好きです。あなたは？", ownSentence, "Japanese"),
                "AI 자기 문장을 배열시키는 퀴즈는 거부한다");
        assertFalse(OpenAiStoryService.isAnswerEchoedInMessage("何が好き？", ownSentence, "Japanese"));

        assertEquals("ねえ、コーヒーは好きなの？",
                OpenAiStoryService.removeDuplicateTrailingQuestion("コーヒーは好き？ねえ、コーヒーは好きなの？", "Japanese"),
                "같은 질문이 두 번 붙으면 앞 것을 지운다");
        assertEquals("今日は寒いね。コーヒーは好き？",
                OpenAiStoryService.removeDuplicateTrailingQuestion("今日は寒いね。コーヒーは好き？", "Japanese"),
                "질문이 아닌 앞 문장은 그대로 둔다");
        assertEquals("コーヒーは好き？", OpenAiStoryService.removeDuplicateTrailingQuestion("コーヒーは好き？", "Japanese"));
    }

    @Test
    @DisplayName("대상 언어로 써야 할 퀴즈 필드가 다른 문자로 쓰이면 걸러낸다")
    void detectsWrongScriptInTargetLanguageFields() {
        // 실측(2026-09-22): asked 를 한국어로 써서 대사 끝에 한국어 질문이 붙었다
        assertEquals("asked", OpenAiStoryService.wrongScriptField("Japanese",
                Map.of("asked", "왜 호타루 근처에서 조용해야 하는지 말해 줄래?", "correct_answer", "静かにしてね")));

        // 실측(2026-09-22): asked 가 영어로 와서 일본어 대사 끝에 영어 문장이 붙었다
        assertEquals("asked", OpenAiStoryService.wrongScriptField("Japanese",
                Map.of("asked", "Is that friend from the Chiikawa series?", "correct_answer", "はい、そうです")));

        // 실측(2026-09-22): 정답에 한글이 섞여 학습자가 만들 수 없는 문제가 됐다
        assertEquals("correct_answer", OpenAiStoryService.wrongScriptField("Chinese (Simplified)",
                Map.of("asked", "您看完后感觉怎么样？", "correct_answer", "看完中경삼림我感觉很特别")));
        assertEquals("tiles", OpenAiStoryService.wrongScriptField("Chinese (Simplified)",
                Map.of("asked", "您看完后感觉怎么样？", "correct_answer", "我感觉很特别",
                        "tiles", List.of("我感觉", "很特别", "中경삼림"))));

        assertNull(OpenAiStoryService.wrongScriptField("Japanese",
                Map.of("asked", "コーヒーは好き？", "correct_answer", "うん、好きだよ",
                        "question", "'응, 좋아해'를 뜻하는 표현은?", "hint", "좋아한다는 뜻이에요")),
                "한국어로 써야 하는 question·hint 는 검사 대상이 아니다");
        assertNull(OpenAiStoryService.wrongScriptField("Japanese",
                Map.of("asked", "ポーカーは好き？", "correct_answer", "ロイヤルストレートフラッシュだよ")),
                "가타카나 외래어는 로마자가 아니다");
        assertNull(OpenAiStoryService.wrongScriptField("English",
                Map.of("asked", "Do you play often?", "correct_answer", "Yes, quite often")),
                "영어 세션의 로마자는 정상이다");
    }

    @Test
    @DisplayName("단어배열에서 타일을 모두 쓴 다른 어순은 모델 판정에 맡긴다")
    void wordArrangeRearrangementGoesToTheModel() {
        // 실측(2026-09-22): 모델이 "다른 어순"이라며 일본어로 성립하지 않는 배열까지 정답 목록에 넣었다
        Map<String, Object> japanese = Map.of("quiz_type", "word_arrange",
                "reply_meaning", "베팅을 계속하겠습니다",
                "question", "'베팅을 계속하겠습니다'가 되도록 단어를 배열해 보세요.",
                "correct_answer", "ベットを続けます",
                "acceptable_answers", List.of("続けますベットを"),
                "options", List.of("ベットを続けます", "ベットをやめます"),
                "tiles", List.of("ベットを", "続けます"));
        Map<String, Object> fixed = OpenAiStoryService.sanitizeQuiz(japanese, "Japanese");

        assertNull(fixed.get("acceptable_answers"), "믿을 수 없는 어순 목록은 쓰지 않는다");
        assertNull(fixed.get("options"), "단어배열에 객관식 보기는 필요 없다");

        assertEquals("correct", OpenAiStoryService.classifyAnswer(fixed, "ベットを 続けます", "Japanese"));
        assertNull(OpenAiStoryService.classifyAnswer(fixed, "続けます ベットを", "Japanese"),
                "타일을 모두 쓴 다른 어순은 서버가 확정하지 않고 모델이 판정한다");
        assertEquals(OpenAiStoryService.NOT_ATTEMPT,
                OpenAiStoryService.classifyAnswer(fixed, "カードを引きます", "Japanese"));

        Map<String, Object> english = Map.of("quiz_type", "word_arrange",
                "correct_answer", "I usually eat out", "tiles", List.of("I", "usually", "eat", "out"));
        assertNull(OpenAiStoryService.classifyAnswer(english, "Usually I eat out", "English"),
                "영어도 같은 규칙으로 모델에 맡긴다");
        assertEquals("incorrect", OpenAiStoryService.classifyAnswer(english, "I eat out", "English"),
                "타일을 남긴 배열은 오답이다");
    }

    @Test
    @DisplayName("한국어 안내문에 정답이 그대로 적혀 있으면 걸러낸다")
    void detectsAnswerShownInsideTheQuestion() {
        // 실측(2026-09-22): reply_meaning 이 한국어·일본어 혼용이라 안내문에 정답이 통째로 박혔고,
        // 학습자는 안내문을 베껴 썼는데 미시도 처리됐다
        Map<String, Object> leaked = Map.of("quiz_type", "subjective",
                "question", "'나는駅前のカフェに行きたいよ。'를 뜻하는 표현은?",
                "correct_answer", "駅前のカフェ", "acceptable_answers", List.of("駅前のカフェ"));
        assertEquals("駅前のカフェ", OpenAiStoryService.answerEchoedInQuestion(leaked, "Japanese"));

        Map<String, Object> fine = Map.of("quiz_type", "subjective",
                "question", "'역 앞 카페'를 뜻하는 표현은?", "correct_answer", "駅前のカフェ");
        assertNull(OpenAiStoryService.answerEchoedInQuestion(fine, "Japanese"));

        Map<String, Object> english = Map.of("quiz_type", "multiple_choice",
                "question", "'녹차'를 뜻하는 green tea 표현은?", "correct_answer", "green tea");
        assertEquals("green tea", OpenAiStoryService.answerEchoedInQuestion(english, "English"));
        assertNull(OpenAiStoryService.answerEchoedInQuestion(
                Map.of("quiz_type", "multiple_choice", "question", "'녹차'를 뜻하는 표현은?",
                        "correct_answer", "green tea"), "English"));
    }

    @Test
    @DisplayName("한국어로 답하면 오답으로 확정한다")
    void koreanSubmissionIsGradedIncorrect() {
        // 실측(2026-09-22): "네 맞아요" 를 모델이 들쭉날쭉 판정해 기회 계산이 어긋났다
        Map<String, Object> quiz = Map.of("quiz_type", "subjective",
                "correct_answer", "はい", "acceptable_answers", List.of("はい", "うん"));
        assertEquals("incorrect", OpenAiStoryService.classifyAnswer(quiz, "네 맞아요", "Japanese"));
        assertEquals("incorrect", OpenAiStoryService.classifyAnswer(quiz, "네", "Japanese"));
        assertEquals("correct", OpenAiStoryService.classifyAnswer(quiz, "はい", "Japanese"));
        assertTrue(OpenAiStoryService.isKoreanSubmission("Japanese", "그거 무슨 뜻이야?"));
        assertFalse(OpenAiStoryService.isKoreanSubmission("Japanese", "はい"));
    }

    @Test
    @DisplayName("맞장구만 묻는 퀴즈와 반복 질문을 걸러낸다")
    void detectsFillerAnswersAndRepeatedQuestions() {
        // 실측(2026-09-22): 정답이 "はい" 하나인 퀴즈에 네 턴을 썼다
        assertEquals("はい", OpenAiStoryService.fillerOnlyAnswer(Map.of("correct_answer", "はい")));
        assertEquals("はい。", OpenAiStoryService.fillerOnlyAnswer(Map.of("correct_answer", "はい。")));
        assertNull(OpenAiStoryService.fillerOnlyAnswer(Map.of("correct_answer", "はい、そうします")));

        // 실측(2026-09-22): "次にどうしますか？" 가 그대로 두 번, 사실상 같은 질문이 네 번 나왔다
        List<String> asked = List.of("次にどうしますか？");
        assertTrue(OpenAiStoryService.isQuestionAlreadyAsked(List.of(), asked,
                Map.of("asked", "次にどうしますか？"), "Japanese"));
        assertTrue(OpenAiStoryService.isQuestionAlreadyAsked(List.of(), asked,
                Map.of("asked", "次はどうしますか？"), "Japanese"), "어미만 바꾼 같은 질문도 잡는다");
        assertFalse(OpenAiStoryService.isQuestionAlreadyAsked(List.of(), asked,
                Map.of("asked", "コーヒーは好きですか？"), "Japanese"));

        // 같은 내용을 어순만 바꿔 다시 출제하는 것도 잡는다
        assertTrue(OpenAiStoryService.sameContentNoSpace("続けてベットします", "ベットを続けます"));
        assertFalse(OpenAiStoryService.sameContentNoSpace("ベットを続けます", "カードを引きます"));
    }

    @Test
    @DisplayName("정답과 어긋나는 글자 수 힌트는 버린다")
    void dropsHintThatContradictsTheAnswer() {
        // 실측(2026-09-22): 정답이 한 글자 '夜' 인데 힌트가 "2글자" 라, 힌트를 따른 학습자가 오답 처리됐다
        Map<String, Object> misleading = Map.of("quiz_type", "subjective", "reply_meaning", "밤에",
                "question", "'밤에'를 뜻하는 표현은?", "correct_answer", "夜",
                "acceptable_answers", List.of("夜"), "hint", "2글자로 밤을 뜻하는 단어야");
        assertNull(OpenAiStoryService.sanitizeQuiz(misleading, "Japanese").get("hint"));

        Map<String, Object> koreanNumber = Map.of("quiz_type", "subjective", "reply_meaning", "처음이야",
                "question", "'처음이야'를 뜻하는 표현은?", "correct_answer", "first time",
                "acceptable_answers", List.of("first time"), "hint", "세 단어로 된 표현이에요");
        assertNull(OpenAiStoryService.sanitizeQuiz(koreanNumber, "English").get("hint"),
                "두 단어인데 세 단어라고 하면 버린다");

        Map<String, Object> correctHint = Map.of("quiz_type", "subjective", "reply_meaning", "자주",
                "question", "'자주'를 뜻하는 표현은?", "correct_answer", "often",
                "acceptable_answers", List.of("often"), "hint", "o로 시작하는 한 단어예요");
        assertEquals("o로 시작하는 한 단어예요",
                OpenAiStoryService.sanitizeQuiz(correctHint, "English").get("hint"), "맞는 힌트는 그대로 둔다");
    }

    @Test
    @DisplayName("대사가 한국어로 오면 대상 언어로 다시 쓰라는 지시문을 붙인다")
    void buildsLanguageRetryDirective() {
        String directive = service.buildLanguageRetryDirective(japaneseSession());

        assertTrue(directive.contains("came back in Korean"));
        assertTrue(directive.contains("Japanese"), "대상 언어가 지시문에 들어가야 한다");
        assertTrue(directive.contains("ゆい"), "캐릭터 이름으로 역할을 상기시킨다");
        assertTrue(directive.contains("never correct the learner like a teacher"),
                "캐릭터가 한국어 선생으로 바뀌던 실측 사고의 재발 방지");
    }

    @Test
    @DisplayName("asked 는 대상 언어로 쓰라는 규칙이 프롬프트에 있다")
    void promptRequiresAskedInTargetLanguage() {
        String prompt = service.buildStaticSystemPrompt(japaneseSession());

        assertTrue(prompt.contains("Write \"asked\" IN THE TARGET LANGUAGE"));
        assertTrue(prompt.contains("written IN THE TARGET LANGUAGE (never Korean)"));
        assertTrue(prompt.contains("Leave acceptable_answers OUT for this format"),
                "단어배열 어순 목록은 모델이 미리 적지 않는다 (판정은 그 턴에)");
        assertTrue(prompt.contains("NOW FILL IN THE REST OF THE QUIZ OBJECT"),
                "question 을 빠뜨리지 않게 하는 단계가 있어야 한다");
        assertTrue(prompt.contains("THE ANSWER MUST CARRY CONTENT"));
        assertTrue(prompt.contains("ANY CLUE YOU GIVE MUST BE TRUE OF correct_answer"));
        assertFalse(prompt.contains("뜻하는 Japanese 표현은"),
                "한국어 질문에 영어 언어명이 박히면 안 된다");
    }

    @Test
    @DisplayName("띄어쓰기 없는 언어의 앵무새 검사는 통째로 이어진 겹침만 본다")
    void noSpaceEchoUsesLongestCommonSubstring() {
        // 실측(2026-09-22, 중국어 세션): 정상 퀴즈가 낱말 겹침 때문에 세 턴 연속 거부돼 퀴즈가 한참 늦게 나왔다
        Map<String, Object> naturalReply = Map.of("quiz_type", "word_arrange",
                "correct_answer", "我经常听嘻哈音乐",
                "tiles", List.of("我", "经常", "听", "嘻哈音乐"));
        assertFalse(OpenAiStoryService.isAnswerEchoedInMessage(
                        "您喜欢嘻哈音乐，真是很有个性。请告诉我，您经常听什么样的音乐？",
                        naturalReply, "Chinese (Simplified)"),
                "중국어는 대답이 질문의 낱말을 되받는 것이 자연스럽다");

        // AI 가 자기 문장을 그대로 배열시키는 진짜 앵무새는 그대로 걸러야 한다
        Map<String, Object> ownSentence = Map.of("quiz_type", "word_arrange",
                "correct_answer", "是的，我去过加利福尼亚",
                "tiles", List.of("是的", "我", "去过", "加利福尼亚"));
        assertTrue(OpenAiStoryService.isAnswerEchoedInMessage(
                "是的，我去过加利福尼亚。那里风景优美，文化多样。", ownSentence, "Chinese (Simplified)"));

        assertEquals(3, OpenAiStoryService.longestCommonSubstringLength("我经常听嘻哈音乐", "您经常听什么样的音乐"),
                "이어진 공통 구간은 '经常听' 세 글자뿐이다");
        assertEquals(0, OpenAiStoryService.longestCommonSubstringLength("", "커피"));
        assertEquals(0, OpenAiStoryService.longestCommonSubstringLength("커피", null));
    }

    @Test
    @DisplayName("일본어 대사에 섞인 한글 문장은 전각 문장부호 기준으로 잘라 떼어 낸다")
    void stripsHangulFromJapaneseMessage() {
        assertEquals("コーヒーは好き？", OpenAiStoryService.stripHangulSentences("Japanese", "コーヒーは好き？커피 좋아해?"));
        assertTrue(OpenAiStoryService.hasUnexpectedHangul("Japanese", "コーヒーは好き？커피 좋아해?"));
        assertFalse(OpenAiStoryService.hasUnexpectedHangul("Japanese", "コーヒーは好き？"));
    }

    @Test
    @DisplayName("일본어 단어배열 정답은 공백을 넣어 보내도 채점 지시문이 정답으로 내려간다")
    void japaneseWordArrangeIsGradedCorrectInTheDirective() {
        StorySession session = japaneseSession();
        session.recordQuiz(Map.of("quiz_type", "word_arrange",
                "question", "'커피가 좋아'가 되도록 단어를 배열해 보세요.",
                "reply_meaning", "커피가 좋아",
                "asked", "コーヒーは好き？",
                "correct_answer", "俺の必殺技はスローストライクだ",
                "tiles", List.of("俺の", "必殺技は", "スローストライクだ")));
        session.incrementTurnsSinceLastQuiz();

        // 앱이 타일을 공백으로 이어 보낸 형태 (실측 2026-09-22)
        String submitted = "俺の 必殺技は スローストライクだ";
        assertEquals("correct", OpenAiStoryService.serverVerdict(session, submitted));

        String prompt = service.buildTurnSystemPrompt(session, submitted);
        assertTrue(prompt.contains("ALREADY graded this answer as CORRECT"),
                "서버 판정과 지시문 판정이 엇갈리면 안 된다");
        assertFalse(prompt.contains("ALREADY graded this answer as INCORRECT"));

        assertEquals("俺の必殺技はスローストライクだ",
                OpenAiStoryService.normalizeSubmissionForPrompt(session, submitted),
                "프롬프트 사본에서는 타일 구분용 공백을 뗀다");
        assertEquals("知るわけないやん",
                OpenAiStoryService.normalizeSubmissionForPrompt(session, "知るわけないやん"),
                "답안이 아닌 입력은 그대로 둔다");
    }

    @Test
    @DisplayName("채점 지시문에 학습자 답의 한국어 뜻이 들어간다")
    void gradingDirectiveCarriesReplyMeaning() {
        StorySession session = japaneseSession();
        session.recordQuiz(Map.of("quiz_type", "multiple_choice",
                "question", "'네가 더 지독해'를 뜻하는 표현은?",
                "reply_meaning", "네가 더 지독해",
                "asked", "どっちがもっとひどいと思う？",
                "correct_answer", "お前のほうがひどい",
                "options", List.of("俺のほうがひどい", "お前のほうがひどい", "どっちもひどい")));
        session.incrementTurnsSinceLastQuiz();

        String prompt = service.buildTurnSystemPrompt(session, "お前のほうがひどい");
        assertTrue(prompt.contains("What they are telling you with it (Korean): 네가 더 지독해"));
    }

    @Test
    @DisplayName("일본어·중국어 자기 질문과 언어 메타 질문을 걸러낸다")
    void detectsCjkSelfAndMetaQuestions() {
        assertTrue(OpenAiStoryService.isQuestionAboutAiItself(Map.of("asked", "俺のよく使う必殺技は何だと思う？")));
        assertTrue(OpenAiStoryService.isQuestionAboutAiItself(Map.of("asked", "俺の必殺技に対抗するために使う必殺技は何だ？")));
        assertTrue(OpenAiStoryService.isQuestionAboutAiItself(Map.of("asked", "我的必杀技是什么？")));
        assertFalse(OpenAiStoryService.isQuestionAboutAiItself(Map.of("asked", "温かいの、それとも冷たいの？")),
                "보기를 함께 제시한 선택 질문은 통과");
        assertFalse(OpenAiStoryService.isQuestionAboutAiItself(Map.of("asked", "コーヒーは好き？")));

        assertTrue(OpenAiStoryService.isMetaLanguageQuestion("", Map.of("asked", "これ、日本語で何て言う？")));
        assertTrue(OpenAiStoryService.isMetaLanguageQuestion("", Map.of("asked", "用中文怎么说？")));
        assertFalse(OpenAiStoryService.isMetaLanguageQuestion("", Map.of("asked", "コーヒーにする？")));
    }

    @Test
    @DisplayName("대화에 나온 적 없는 가타카나 이름을 주관식 정답으로 내면 거부 대상이다")
    void rejectsInventedKatakanaSubjectiveAnswer() {
        StorySession session = japaneseSession();
        session.addMessage("assistant", "俺に勝てると思うなよ。");

        Map<String, Object> invented = Map.of("quiz_type", "subjective",
                "acceptable_answers", List.of("シャドウバースト"), "correct_answer", "シャドウバースト");
        assertEquals("シャドウバースト", OpenAiStoryService.unknownInventedTerm(session, invented));

        session.addMessage("assistant", "俺の技はシャドウバーストだ。");
        assertNull(OpenAiStoryService.unknownInventedTerm(session, invented),
                "AI 가 먼저 말한 이름은 퀴즈로 쓸 수 있다");

        Map<String, Object> multipleChoice = Map.of("quiz_type", "multiple_choice",
                "correct_answer", "シャドウクロー", "options", List.of("シャドウクロー", "ライトニング"));
        assertNull(OpenAiStoryService.unknownInventedTerm(session, multipleChoice),
                "보기가 주어지는 객관식은 대상이 아니다");

        StorySession english = newSession();
        assertNull(OpenAiStoryService.unknownInventedTerm(english, invented), "일본어 세션에만 적용한다");
    }

    @Test
    @DisplayName("정답이 학습자의 대사가 아니라 제3자 서술이면 걸러낸다")
    void detectsThirdPersonNarration() {
        assertTrue(OpenAiStoryService.isThirdPersonNarration(
                Map.of("reply_meaning", "화가 난다", "correct_answer", "彼は怒っている")));
        assertTrue(OpenAiStoryService.isThirdPersonNarration(
                Map.of("reply_meaning", "그는 화가 났어", "correct_answer", "He is angry")));
        assertTrue(OpenAiStoryService.isThirdPersonNarration(
                Map.of("reply_meaning", "상대는 도망칠 거야", "correct_answer", "他会跑")));
        assertFalse(OpenAiStoryService.isThirdPersonNarration(
                Map.of("reply_meaning", "나는 화가 나", "correct_answer", "俺は怒ってる")));
        assertFalse(OpenAiStoryService.isThirdPersonNarration(
                Map.of("reply_meaning", "차가운 걸로", "correct_answer", "冷たいの")));
    }

    @Test
    @DisplayName("한국어가 아닌 question 은 reply_meaning 으로 다시 쓰고, 한국어가 아닌 hint 는 버린다")
    void rewritesNonKoreanLearnerFacingFields() {
        Map<String, Object> japaneseFields = Map.of("quiz_type", "subjective",
                "reply_meaning", "필살기 이름",
                "question", "相手の必殺技に対抗するために使う必殺技は何ですか？",
                "hint", "必殺技の名前、シャドウ＋何か",
                "acceptable_answers", List.of("シャドウバースト"));
        Map<String, Object> fixed = OpenAiStoryService.sanitizeQuiz(japaneseFields, "Japanese");

        assertEquals("'필살기 이름'를 뜻하는 표현은?", fixed.get("question"));
        assertNull(fixed.get("hint"), "한국어가 아닌 힌트는 학습자에게 쓸모가 없다");

        Map<String, Object> arrange = Map.of("quiz_type", "word_arrange",
                "reply_meaning", "커피가 좋아", "question", "コーヒーが好き",
                "correct_answer", "コーヒーが好き", "tiles", List.of("コーヒーが", "好き"));
        assertEquals("'커피가 좋아'가 되도록 단어를 배열해 보세요.",
                OpenAiStoryService.sanitizeQuiz(arrange, "Japanese").get("question"));

        Map<String, Object> korean = Map.of("quiz_type", "subjective",
                "reply_meaning", "자주", "question", "'자주'를 뜻하는 표현은?", "hint", "o로 시작해요",
                "acceptable_answers", List.of("often"));
        Map<String, Object> untouched = OpenAiStoryService.sanitizeQuiz(korean, "English");
        assertEquals("'자주'를 뜻하는 표현은?", untouched.get("question"));
        assertEquals("o로 시작해요", untouched.get("hint"));
    }

    @Test
    @DisplayName("한글 비율로 story_so_far 메모가 한국어인지 판별한다")
    void measuresHangulRatio() {
        assertTrue(OpenAiStoryService.hangulRatio("서로 깐죽거리며 디스배틀이 시작됐다") > 0.9);
        assertTrue(OpenAiStoryService.hangulRatio("相手が必殺技はスローストライクだと言った") < 0.5);
        assertEquals(0, OpenAiStoryService.hangulRatio(""));
        assertTrue(OpenAiStoryService.hasHangul("커피"));
        assertFalse(OpenAiStoryService.hasHangul("コーヒー"));
    }

    @Test
    @DisplayName("오답 1~2회째는 정답을 공개하지 않고 힌트만 주라고 지시한다")
    void wrongAttemptsGiveHintsOnly() {
        StorySession session = newSession();
        session.recordQuiz(Map.of("quiz_type", "word_arrange", "question", "배열", "correct_answer", "I usually eat out",
                "tiles", List.of("out", "I", "eat", "usually")));
        session.incrementTurnsSinceLastQuiz();

        String prompt = service.buildTurnSystemPrompt(session, "I eat usually out");
        assertTrue(prompt.contains("DO NOT REVEAL THE CORRECT ANSWER YET"));
        assertTrue(prompt.contains("NEVER explain the word order"),
                "어순 설명이 틀려 학습자를 헷갈리게 하던 실측 사고의 재발 방지");
        assertFalse(prompt.contains("LAST allowed attempt"));

        session.recordWrongAttempt();
        session.recordWrongAttempt();
        String last = service.buildTurnSystemPrompt(session, "I eat usually out");
        assertTrue(last.contains("LAST allowed attempt"));
        assertTrue(last.contains("REVEAL the correct expression"));
    }

    @Test
    @DisplayName("프롬프트에 베낄 수 있는 장면 예시가 하나도 남아 있지 않다")
    void staticPromptHasNoCopyableSceneExamples() {
        String prompt = service.buildStaticSystemPrompt(newSession())
                + service.buildOpeningSystemPrompt(newSession())
                + service.buildContinuationSystemPrompt(newSession())
                + service.buildTurnSystemPrompt(newSession(), "안녕");

        // 실측(2026-09-22, 일본어 디스배틀 세션): 프롬프트의 "창가 자리" 예시가 장면과 무관하게 퀴즈로 나왔다
        for (String leaked : List.of("창가 자리", "물레", "접시로 할래", "주로 저녁에 연습해", "처음이야",
                                     "딸기 케이크", "감자튀김", "보스 명령이야", "window seat",
                                     "fries or coleslaw", "pottery", "strawberry cake")) {
            assertFalse(prompt.contains(leaked), "프롬프트에 베낄 수 있는 장면 예시가 남아 있다: " + leaked);
        }
    }

    @Test
    @DisplayName("학습자가 알 수 없는 것을 묻지 말라는 규칙과 한국어 필드 규칙이 들어간다")
    void promptForbidsUnknowableQuizAndDemandsKoreanFields() {
        String prompt = service.buildStaticSystemPrompt(newSession());

        assertTrue(prompt.contains("NEVER QUIZ SOMETHING ONLY YOU COULD KNOW"));
        assertTrue(prompt.contains("The learner cannot read your mind"));
        assertTrue(prompt.contains("SAY IT YOURSELF in ai_message first"),
                "지어낸 이름은 먼저 말한 뒤에야 퀴즈로 쓸 수 있다");
        assertTrue(prompt.contains("Never a narration about a third"),
                "제3자 서술을 정답으로 내지 말라는 규칙이 있어야 한다");
        assertTrue(prompt.contains("EVERY EXPLANATORY FIELD IS WRITTEN IN KOREAN"));
    }

    @Test
    @DisplayName("퀴즈 재질문 금지와 재생성 지시문이 들어간다")
    void noReaskRuleAndCorrectionDirective() {
        String prompt = service.buildTurnSystemPrompt(newSession(), "감자튀김 좋다");
        assertTrue(prompt.contains("NEVER RE-ASK WHAT THEY ALREADY TOLD YOU"));
        assertTrue(prompt.contains("If their latest message already contains the answer, the quiz is wrong"));
        assertFalse(prompt.contains("[\"iced\", \"boiled\", \"hot\"]"), "베낄 수 있는 보기 예시는 없어야 한다");
        assertFalse(prompt.contains("correct_answer: \"often\""), "영어 예시 답은 프롬프트에 없어야 한다");
        assertFalse(prompt.contains("acceptable_answers: [\"often\"]"));
        assertTrue(prompt.contains("Is the answer absent from your own question?"));

        StorySession pending = newSession();
        pending.recordQuiz(Map.of("quiz_type", "subjective", "question", "'처음이야'를 뜻하는 표현은?",
                "asked", "Have you tried the wheel before?", "acceptable_answers", List.of("first time")));
        pending.incrementTurnsSinceLastQuiz();
        assertTrue(service.buildTurnSystemPrompt(pending, "banana").contains("Your in-story question it answers: Have you tried the wheel before?"),
                "채점 턴에는 AI 가 실제로 한 질문을 다시 알려줘야 한다");

        String correction = service.buildCorrectionDirective("the answer repeats a tested expression", "correct");
        assertTrue(correction.contains("REJECTED BY THE SERVER"));
        assertTrue(correction.contains("the answer repeats a tested expression"));
        assertTrue(correction.contains("keep \"answer_result\" as \"correct\""));
    }

    @Test
    @DisplayName("학습자가 방금 한 말을 그대로 퀴즈로 되묻는지 판별한다")
    void detectsReaskOfUserMessage() {
        assertTrue(OpenAiStoryService.isReaskOfRecentUserMessage(List.of("응 주로 미드 해"),
                Map.of("reply_meaning", "주로 미드 해", "question", "'주로 미드 해'를 뜻하는 영어 표현은?")));
        assertTrue(OpenAiStoryService.isReaskOfRecentUserMessage(List.of("요즘은 바빠서 주말에만 해"),
                Map.of("reply_meaning", "주로 주말에만 해", "question", "'주로 주말에만 해'를 뜻하는 표현은?")));
        assertTrue(OpenAiStoryService.isReaskOfRecentUserMessage(List.of("이번 주에 좋아하는 가수 콘서트를 가", "바운디라는 가순데 알아?"),
                Map.of("reply_meaning", "이번 주말에 바운디 콘서트 간다", "question", "'이번 주말에 바운디 콘서트 갈 거야'를 영어로?")),
                "그 전 메시지와 두 단어 이상 겹치면 되묻기다");
        assertTrue(OpenAiStoryService.isReaskOfRecentUserMessage(List.of("차가운거 가자"),
                Map.of("reply_meaning", "차가운 걸로", "question", "'차가운 걸로'를 뜻하는 표현은?")));

        assertFalse(OpenAiStoryService.isReaskOfRecentUserMessage(List.of("감자튀김 좋다"),
                Map.of("reply_meaning", "케첩으로 할래", "question", "'케첩'을 뜻하는 영어 단어는?")),
                "새 정보를 묻는 퀴즈는 통과한다");
        assertFalse(OpenAiStoryService.isReaskOfRecentUserMessage(List.of("게임 얘기 할래?"),
                Map.of("reply_meaning", "주말마다 해", "question", "'주말마다'를 뜻하는 표현은?")));
        assertFalse(OpenAiStoryService.isReaskOfRecentUserMessage(List.of("게임 얘기 할래?"),
                Map.of("reply_meaning", "주로 핸드폰에서 게임해", "question", "'주로 핸드폰에서 게임해'를 영어로?")),
                "주제 단어 하나만 겹치는 후속 질문은 통과한다");
        assertFalse(OpenAiStoryService.isReaskOfRecentUserMessage(List.of("응 주로 미드 해"),
                Map.of("reply_meaning", "저녁이나 주말에 게임을 주로 해", "question", "'저녁이나 주말에 게임을 주로 해'를 영어로?")),
                "'주로' 같은 정도 부사만 겹치는 건 되묻기가 아니다");
        assertTrue(OpenAiStoryService.isReaskOfRecentUserMessage(List.of("응 주로 미드 해"),
                Map.of("reply_meaning", "미드 라인을 선호해", "question", "'미드 라인을 선호해'를 영어로?")));
        assertFalse(OpenAiStoryService.isReaskOfRecentUserMessage(List.of(), Map.of("reply_meaning", "주로 미드 해")));
    }

    @Test
    @DisplayName("정적 블록은 턴이 지나도 같고, 이번 턴 블록에 이미 아는 것 목록이 들어간다")
    void staticBlockIsStableAndTurnBlockListsKnownFacts() {
        StorySession session = newSession();
        session.setSpeechLevel("반말");
        String staticBefore = service.buildStaticSystemPrompt(session);

        session.addMessage("user", "치킨");
        session.incrementTurnsSinceLastQuiz();
        session.addAssistantMessage("Chicken it is!", "치킨 좋지!");
        session.addMessage("user", "난 기본이 좋더라");
        session.incrementTurnsSinceLastQuiz();
        session.recordQuiz(Map.of("quiz_type", "multiple_choice", "question", "q", "correct_answer", "ketchup",
                "asked", "Do you want ketchup or mayo with your fries?"));
        session.clearPendingQuiz();
        session.incrementTurnsSinceLastQuiz();
        session.incrementTurnsSinceLastQuiz();

        assertEquals(staticBefore, service.buildStaticSystemPrompt(session), "정적 블록은 세션 중 바뀌면 안 된다 (프롬프트 캐시)");
        assertFalse(staticBefore.contains("THIS TURN:"));
        assertTrue(staticBefore.contains("\"learner_told_me\""));
        assertTrue(staticBefore.contains("\"next_beat\""));
        assertTrue(staticBefore.contains("Is the answer absent from your own question?"));

        String turn = service.buildTurnDirective(session, "매운것도 좋아하긴 해");
        assertTrue(turn.startsWith("THIS TURN:"));
        assertTrue(turn.contains("* \"치킨\""));
        assertTrue(turn.contains("* \"난 기본이 좋더라\""));
        assertTrue(turn.contains("* \"Do you want ketchup or mayo with your fries?\""), "퀴즈로 물었던 질문이 목록에 있어야 한다");
        assertTrue(turn.contains("Expressions already tested (never the focus or answer of a quiz again): "));
        assertTrue(turn.contains("The learner's latest message: \"매운것도 좋아하긴 해\""));
        assertTrue(turn.contains("REQUIRED QUIZ TYPE"), "2턴이 지났으니 퀴즈 턴이어야 한다");
    }

    @Test
    @DisplayName("되묻기 검사는 최근 5개 발화까지 본다 (두세 턴 전 말도 두 단어 이상 겹치면 되묻기)")
    void reaskCheckLooksBackFiveMessages() {
        List<String> recent = List.of("응 주로 미드 해", "요즘은 바빠서 주말에만 해", "인정해", "아 배고프다");
        assertTrue(OpenAiStoryService.isReaskOfRecentUserMessage(recent,
                Map.of("reply_meaning", "주말에만 해", "question", "'주말에만 해'를 영어로?")),
                "세 턴 전에 한 말('주말에만 해')을 되묻는 건 안 된다");
        assertFalse(OpenAiStoryService.isReaskOfRecentUserMessage(recent,
                Map.of("reply_meaning", "짭짤한 거 먹고 싶어", "question", "'짭짤한 거'를 뜻하는 표현은?")));
    }

    @Test
    @DisplayName("언어 메타 질문('뭐라고 말하겠어?')을 판별한다")
    void detectsMetaLanguageQuestion() {
        assertTrue(OpenAiStoryService.isMetaLanguageQuestion("Great. Now, what would you say if you want to go to the yard?",
                Map.of("asked", "What would you say if you want to go to the prison yard after the visit?")));
        assertTrue(OpenAiStoryService.isMetaLanguageQuestion("Fine. During this visit, what question should I ask you?",
                Map.of("asked", "What question should I ask you during this visit?")));
        assertTrue(OpenAiStoryService.isMetaLanguageQuestion("So how do you say that in English?", Map.of()));
        assertFalse(OpenAiStoryService.isMetaLanguageQuestion("Did the boss really send you?",
                Map.of("asked", "Did the boss really send you?")));
        assertFalse(OpenAiStoryService.isMetaLanguageQuestion("What do you want to drink?",
                Map.of("asked", "What do you want to drink?")));
    }

    @Test
    @DisplayName("대사 끝에 같은 질문이 두 번 붙으면 앞의 것을 지운다")
    void removesDuplicateTrailingQuestion() {
        assertEquals("Chicken sounds good. Do you want me to bring cola or juice with the chicken?",
                OpenAiStoryService.removeDuplicateTrailingQuestion(
                        "Chicken sounds good. Now, what drink do you want me to bring with it? Cola or juice? Do you want me to bring cola or juice with the chicken?"));
        assertEquals("You think it's easy because I'm restrained, huh? Fine. What question should I ask you during this visit?",
                OpenAiStoryService.removeDuplicateTrailingQuestion(
                        "You think it's easy because I'm restrained, huh? Fine. During this visit, what question should I ask you? What question should I ask you during this visit?"));
        String clean = "Yeah, I remember Selena. Did you get the letter I sent you recently?";
        assertEquals(clean, OpenAiStoryService.removeDuplicateTrailingQuestion(clean), "겹치지 않으면 그대로");
        assertEquals("Short one?", OpenAiStoryService.removeDuplicateTrailingQuestion("Short one?"));
    }

    @Test
    @DisplayName("객관식 질문에 정답의 한국어 뜻이 없으면 reply_meaning 으로 다시 쓴다")
    void rewritesMultipleChoiceQuestionWithoutMeaning() {
        Map<String, Object> vague = Map.of("quiz_type", "multiple_choice", "reply_meaning", "응, 받았어",
                "question", "'네가 받은 편지 내용'에 대해 대답할 때 쓸 표현은?", "correct_answer", "Yes, I got it");
        assertEquals("'응, 받았어'를 뜻하는 표현은?", OpenAiStoryService.ensureQuestionQuotesMeaning(vague).get("question"));

        Map<String, Object> fine = Map.of("quiz_type", "multiple_choice", "reply_meaning", "차가운 걸로",
                "question", "'차가운 걸로'를 뜻하는 표현은?", "correct_answer", "iced");
        assertEquals("'차가운 걸로'를 뜻하는 표현은?", OpenAiStoryService.ensureQuestionQuotesMeaning(fine).get("question"));

        Map<String, Object> subjective = Map.of("quiz_type", "subjective", "reply_meaning", "꽤 자주 해",
                "question", "'자주'를 뜻하는 o로 시작하는 단어는?", "correct_answer", "often");
        assertEquals("'자주'를 뜻하는 o로 시작하는 단어는?", OpenAiStoryService.ensureQuestionQuotesMeaning(subjective).get("question"),
                "주관식 질문은 단서를 담은 형태가 정상이라 손대지 않는다 (한국어가 아닐 때만 sanitizeQuiz 가 다시 쓴다)");
    }

    @Test
    @DisplayName("reply_meaning 이 대사가 아니라 설명이면 판별한다")
    void detectsDescriptiveReplyMeaning() {
        assertTrue(OpenAiStoryService.isDescriptiveReplyMeaning(Map.of("reply_meaning", "도망친 뒤에 뭘 할지")));
        assertTrue(OpenAiStoryService.isDescriptiveReplyMeaning(Map.of("reply_meaning", "보스가 널 처리하라고 한 이유")));
        assertTrue(OpenAiStoryService.isDescriptiveReplyMeaning(Map.of("reply_meaning", "칼로 찌른 후 나에게 뭘 할지 말하기")));
        assertFalse(OpenAiStoryService.isDescriptiveReplyMeaning(Map.of("reply_meaning", "그래, 보스 명령이야")));
        assertFalse(OpenAiStoryService.isDescriptiveReplyMeaning(Map.of("reply_meaning", "다른 계획이 있어")));
        assertFalse(OpenAiStoryService.isDescriptiveReplyMeaning(Map.of("reply_meaning", "차가운 걸로")));
        assertFalse(OpenAiStoryService.isDescriptiveReplyMeaning(Map.of("correct_answer", "iced")));
    }

    @Test
    @DisplayName("대상 언어가 한국어가 아니면 AI 대사의 한글 문장을 뗀다")
    void stripsHangulSentences() {
        assertEquals("Try again and tell me how you plan to get close to me.",
                OpenAiStoryService.stripHangulSentences("English",
                        "Try again and tell me how you plan to get close to me. 내게 다가가길 원하는 방법은 무엇인가?"));
        assertEquals("Coffee it is!", OpenAiStoryService.stripHangulSentences("English", "Coffee it is!"));
        assertEquals("커피 좋지!", OpenAiStoryService.stripHangulSentences("Korean", "커피 좋지!"));
        assertEquals("전부 한글이면 그대로.", OpenAiStoryService.stripHangulSentences("English", "전부 한글이면 그대로."));
    }

    @Test
    @DisplayName("AI 자신의 행동·감정을 묻는 질문을 판별한다")
    void detectsQuestionAboutAiItself() {
        assertTrue(OpenAiStoryService.isQuestionAboutAiItself(Map.of("asked", "When it's my turn, what will I do?")));
        assertTrue(OpenAiStoryService.isQuestionAboutAiItself(Map.of("asked", "What will I do with the revolver before I pull the trigger?")));
        assertTrue(OpenAiStoryService.isQuestionAboutAiItself(Map.of("asked", "Before pulling the trigger, what feeling hits me hard?")));
        assertFalse(OpenAiStoryService.isQuestionAboutAiItself(Map.of("asked", "Are you going first or should I take the first shot?")),
                "학습자에게 선택을 주는 질문은 통과");
        assertFalse(OpenAiStoryService.isQuestionAboutAiItself(Map.of("asked", "How many bullets will you start with?")));
        assertFalse(OpenAiStoryService.isQuestionAboutAiItself(Map.of("asked", "Did the boss really send you?")));
        assertTrue(service.buildStaticSystemPrompt(newSession()).contains("FORBIDDEN: questions about YOU"));
    }

    @Test
    @DisplayName("몰입 지시와 메타 질문 금지가 정적 프롬프트에 들어간다")
    void immersionRulesInStaticPrompt() {
        String prompt = service.buildStaticSystemPrompt(newSession());
        assertTrue(prompt.contains("STAY IN THE DRAMA"));
        assertTrue(prompt.contains("being stabbed is not \"whatever\""));
        assertTrue(prompt.contains("FORBIDDEN: meta questions about language"));
        assertTrue(prompt.contains("THE LEARNER STEERS"), "학습자가 흐름을 주도한다는 규칙이 있어야 한다");
        assertFalse(prompt.contains("React to what the learner just said in one clause"));
    }

    @Test
    @DisplayName("퀴즈 페이싱: 2턴은 자연스러울 때만(MAY), 3턴부터 필수(MUST), 8턴이면 마무리, 강제 비퀴즈 턴")
    void pacingWindowAndForcedClose() {
        StorySession session = newSession();
        session.incrementTurnsSinceLastQuiz();
        session.incrementTurnsSinceLastQuiz();
        String may = service.buildTurnDirective(session, "hi");
        assertTrue(may.contains("You MAY present a quiz this turn"));
        assertTrue(may.contains("REQUIRED QUIZ TYPE FOR THIS QUIZ"));
        assertFalse(may.contains("You MUST present a quiz"));

        // 2026-09-22: 퀴즈가 너무 늦게 나온다는 실사용 피드백으로 필수 시점을 4턴에서 3턴으로 당겼다
        session.incrementTurnsSinceLastQuiz();
        String must = service.buildTurnDirective(session, "hi");
        assertTrue(must.contains("You MUST present a quiz this turn"));
        assertFalse(OpenAiStoryService.isOverdueForClose(session));

        for (int i = 0; i < 5; i++) {   // 3턴 -> 8턴
            session.incrementTurnsSinceLastQuiz();
        }
        assertTrue(OpenAiStoryService.isOverdueForClose(session), "8턴이면 상한");
        String close = service.buildTurnDirective(session, "hi");
        assertTrue(close.contains("THE SCENE HAS RUN LONG"));
        assertTrue(close.contains("`is_completed: true`"));
        assertFalse(close.contains("REQUIRED QUIZ TYPE"));
        assertTrue(InteractiveStoryService.shouldForceComplete(session), "서버도 같은 턴에 완결 처리한다");

        String plain = service.buildTurnDirective(newSession(), "hi", true);
        assertTrue(plain.contains("NO QUIZ THIS TURN"));
    }

    @Test
    @DisplayName("이야기 메모(story_so_far)가 이번 턴 블록에 들어가고, 대화 주도권 규칙이 정적 프롬프트에 있다")
    void storySoFarAndLearnerSteers() {
        StorySession session = newSession();
        assertTrue(service.buildTurnDirective(session, "hi").contains("STORY SO FAR"));
        assertTrue(service.buildTurnDirective(session, "hi").contains("(nothing yet - the scene just started)"));
        session.setStorySoFar("시험 끝나고 같이 점심 먹기로 함. 7시에 노래방.");
        assertTrue(service.buildTurnDirective(session, "hi").contains("시험 끝나고 같이 점심 먹기로 함. 7시에 노래방."));

        String prompt = service.buildStaticSystemPrompt(session);
        assertTrue(prompt.contains("THE LEARNER STEERS"));
        assertTrue(prompt.contains("\"story_so_far\""));
        assertTrue(prompt.contains("off and return to your previous question."),
                "도발·장난에도 캐릭터로 반응하라는 규칙이 있어야 한다");
        assertFalse(prompt.contains("the next choice in the activity (size, side, seat, time, route)"), "카페 템플릿 목록은 삭제");
        assertTrue(prompt.contains("The question must ask for EXACTLY the expression in acceptable_answers"),
                "주관식은 허용 답 그대로 물어야 한다");
    }

    @Test
    @DisplayName("AI 가 이미 했던 질문을 다시 퀴즈로 내면 판별한다")
    void detectsRepeatedQuestion() {
        List<Map<String, String>> history = List.of(
                Map.of("role", "assistant", "content", "Oh, mid lane main, nice! Do you usually play games in the evening or during the weekend?"),
                Map.of("role", "user", "content", "요즘은 바빠서 주말에만 해"),
                Map.of("role", "assistant", "content", "I get that, weekends are perfect for gaming."));
        assertTrue(OpenAiStoryService.isQuestionAlreadyAsked(history,
                Map.of("asked", "Do you usually play games in the evening or during the weekend?")));
        assertFalse(OpenAiStoryService.isQuestionAlreadyAsked(history,
                Map.of("asked", "Do you prefer playing games alone or with friends?")));
        assertFalse(OpenAiStoryService.isQuestionAlreadyAsked(history, Map.of("correct_answer", "alone")), "asked 가 없으면 검사하지 않는다");
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

        assertTrue(prompt.contains("Quiz count so far: 5 / 10"));
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
