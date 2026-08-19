package com.qring.qring_backend.service.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qring.qring_backend.domain.content.StorySession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OpenAiStoryService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiStoryService.class);

    /** 한 세션에서 출제하는 총 퀴즈 개수. */
    public static final int MAX_QUIZ_COUNT = 5;

    @Value("${qring.openai.api-key:}")
    private String apiKey;

    @Value("${qring.openai.model:gpt-4o-mini}")
    private String modelName;

    private final RestTemplate restTemplate = createRestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 타임아웃 없는 호출이 톰캣 스레드를 무기한 붙잡지 않도록 연결/응답 타임아웃을 건다. */
    private static RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(60));
        return new RestTemplate(factory);
    }

    /**
     * 1단계: 대화 오프닝 생성 - 역할극 세션의 첫 인사를 OpenAI(ChatGPT)로 실시간 생성
     */
    public Map<String, Object> generateOpening(StorySession session) {
        validateApiKey();

        String systemPrompt = buildOpeningSystemPrompt(session);

        try {
            return callOpenAiJson(List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", "Start the conversation now.")
            ));
        } catch (Exception e) {
            log.error("[OpenAI API 호출 오류] 오프닝 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("OpenAI API 호출 실패: " + e.getMessage(), e);
        }
    }

    /** 오프닝용 시스템 프롬프트 조립 (네트워크 호출과 분리되어 단위 테스트 가능). */
    String buildOpeningSystemPrompt(StorySession session) {
        return String.format("""
            You are AI Partner "%s", an adaptive roleplay partner in a language-learning app.
            Situation: %s
            Requested Mood/Tone: %s
            Target Language: %s (User Level Code: %d)

            %s

            Generate the first opening message from you (the AI partner) to start the scenario naturally.
            - Set the scene in one or two short sentences, then end with ONE concrete question the
              learner can actually answer (e.g. what they want to order, where they want to sit).
            - Do not ask several things at once, and do not quiz them yet.
            You MUST return your response formatted strictly as a valid json object with the following fields:
            {
              "ai_message": "Opening line in the target language",
              "translation": "Korean translation at the same speech level as ai_message"
            }
            """, session.getCharacterName(), session.getSituationDescription(), session.getTone(),
                session.getTargetLanguage(), session.getLevelCode(),
                buildSpeechStyleDirective(session.getTone(), session.getSituationDescription()));
    }

    /**
     * 말투 지시문. 어조(따뜻함/격식의 정도)와 높임법(반말/존댓말)을 분리해서,
     * 높임법은 상황이 암시하는 관계로 판단하게 한다.
     * "다정하게"가 곧 반말을 뜻하지는 않기 때문이다.
     */
    private String buildSpeechStyleDirective(String tone, String situationDescription) {
        return String.format("""
            TONE & SPEECH-LEVEL RULES:
            - Requested Tone/Mood: "%s". This describes the EMOTIONAL WARMTH and ATTITUDE of your
              delivery (warm, playful, brisk, professional...). It does NOT by itself decide the
              politeness level.
            - The Korean speech level (반말 vs 존댓말) and the target-language register are decided by
              the RELATIONSHIP implied by the Situation ("%s"), NOT by the tone word:
              * Close friends, peers, classmates, siblings, or an explicitly casual relationship
                -> Korean 반말, relaxed register in the target language.
              * Strangers, first meetings, staff and customer, teacher and student, senior colleague,
                interviewer, or any clear age or status gap
                -> Korean 존댓말 (~요 / ~습니다), polite register in the target language.
              * If the Situation explicitly states how to speak (e.g. "반말로", "편하게 말 놓고",
                "정중하게"), follow that instruction. It overrides the inference above.
              * If the relationship is genuinely unclear, default to 존댓말.
            - A warm tone is fully compatible with 존댓말. "다정하게" toward a stranger or a senior
              means warm, considerate, friendly WORDING. It does NOT mean dropping honorifics.
            - Never force a speech level the relationship would not support. Sounding natural for the
              relationship always wins over matching the tone word literally.
            - Keep the chosen speech level CONSISTENT for the whole session, and keep "translation" at
              the same speech level as "ai_message".
            """, tone, situationDescription);
    }

    /**
     * 2단계: 턴 바이 턴 대화 - 사용자의 대답을 받아 OpenAI(ChatGPT)로 100% 실시간 대화 반응 및 퀴즈 생성
     */
    public Map<String, Object> generateTurnResponse(StorySession session, String userMessage) {
        validateApiKey();

        String systemPrompt = buildTurnSystemPrompt(session, userMessage);

        try {
            // 시스템 프롬프트를 최상단에 배치하고 대화 히스토리 전체 결합
            List<Map<String, String>> fullMessages = new ArrayList<>();
            fullMessages.add(Map.of("role", "system", "content", systemPrompt));
            fullMessages.addAll(session.getChatHistory());

            return callOpenAiJson(fullMessages);
        } catch (Exception e) {
            log.error("[OpenAI API 호출 오류] 턴 대화 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("OpenAI API 호출 실패: " + e.getMessage(), e);
        }
    }

    /** 턴 대화용 시스템 프롬프트 조립 (네트워크 호출과 분리되어 단위 테스트 가능). */
    String buildTurnSystemPrompt(StorySession session, String userMessage) {
        int currentQuizCount = session.getQuizCount();
        int turnsSinceLastQuiz = session.getTurnsSinceLastQuiz();
        boolean quizBudgetLeft = currentQuizCount < MAX_QUIZ_COUNT;
        boolean allowQuiz = turnsSinceLastQuiz >= 2 && quizBudgetLeft;

        String pacingDirective;
        if (allowQuiz) {
            pacingDirective = String.format("PACING RULE: Sufficient dialogue turns have passed (%d turns since last quiz). You SHOULD now present a relevant quiz moment matching the recent context by setting `is_quiz: true`.", turnsSinceLastQuiz);
        } else if (!quizBudgetLeft) {
            pacingDirective = String.format("QUIZ BUDGET EXHAUSTED: All %d quizzes for this session have already been given. YOU MUST SET `is_quiz: false`. Wrap the scenario up naturally and set `is_completed: true`.", MAX_QUIZ_COUNT);
        } else {
            pacingDirective = String.format("STRICT PACING RULE: ONLY %d dialogue turn(s) passed since last quiz/start. YOU MUST SET `is_quiz: false` FOR THIS TURN! Do NOT output a quiz yet. Continue the natural dialogue (A-B-A-B dialogue turn) and ask an engaging follow-up question.", turnsSinceLastQuiz);
        }

        String testedSubjectsDirective = session.getTestedQuizSubjects().isEmpty()
                ? "No quiz topics have been tested yet."
                : "FORBIDDEN ALREADY-TESTED QUIZ TOPICS/WORDS (NEVER TEST OR FOCUS ON ANY OF THESE AGAIN): " + String.join(", ", session.getTestedQuizSubjects());

        String quizContextDirective = buildQuizGradingDirective(session.getPendingQuiz(), userMessage);

        return String.format("""
            You are AI Partner "%s", an adaptive conversation partner in a language-learning app.
            Situation: %s
            Requested Mood/Tone: %s
            Target Language: %s

            %s

            CRITICAL DYNAMIC CONVERSATION & MEMORY RULES:
            1. PREVIOUS TURN QUIZ ANSWER HANDLING:
               %s
            2. CONVERSATION MEMORY & NO REPEAT QUESTIONS:
               - Thoroughly inspect all previous messages in `chatHistory` before responding.
               - YOU MUST REMEMBER ALL DETAILS discussed (e.g. chosen drinks, food, seating preference, museum plans, weekend activities).
               - NEVER repeat a question or ask about a topic you have ALREADY asked about in previous turns! (e.g., if you already asked about favorite cafes, window seats, gallery plans, or weekend plans, DO NOT ask them again).
               - Keep moving the conversation FORWARD to new, natural topics within the scenario.
            3. RESPOND ACCURATELY TO USER'S ACTUAL INPUT:
               - You MUST carefully read the user's latest message ("%s") and respond accurately in character!
               - If the user specifies a preference (e.g. "나는 구석이 좋아", "나는 바닐라라떼가 좋아"), NEVER contradict or ignore their choice. Always accept and adapt to what the user said!
            4. NO ROBOTIC TRANSLATIONESE:
               - NEVER say robotic phrases like "Thanks for answering", "That's a good opinion", or repeat the user's input verbatim.
            5. AI CONVERSATION LEADERSHIP:
               - In standard non-quiz turns (`is_quiz: false`), AI should proactively lead the scene by ending with natural follow-up questions or engaging topics.
               - Keep it fluid and non-forced: NEVER steer or force the conversation topic unnaturally just to create a quiz. Always flow naturally with the user's lead.
            6. STRICT INTER-QUIZ PACING (A-B-A-B-A-B-Quiz):
               - %s
            7. Current Quiz Count Given So Far: %d / %d.
               - Once all %d quizzes are finished, wrap up the scene with `is_completed: true`.

            THE MOST IMPORTANT QUIZ RULE - A QUIZ IS PART OF THE CONVERSATION, NOT A POP-UP TEST:
            - A quiz must be the very thing the learner needs to SAY next in the scene.
            - When `is_quiz` is true, your `ai_message` MUST first ask ONE concrete in-story question.
              The quiz then asks for the expression the learner needs in order to ANSWER that question.
            - Design every quiz so that its correct answer is ALSO a natural, valid reply to that question.
              GOOD: You ask "뭐 마실래?" -> quiz asks "'녹차'를 뜻하는 표현은?" -> answer "green tea".
                    That answer is both the correct answer AND their drink order, so next turn you can
                    simply react to it: "Green tea it is! 녹차 마시고 싶구나. 따뜻한 걸로 줄까?"
              BAD:  You ask "뭐 마실래?" -> quiz asks "'마시다'를 뜻하는 단어는?" -> answer "drink".
                    That does NOT answer your question, so you are forced to say "정답이야!" and then
                    ask what they want to drink all over again. NEVER build a quiz like this.
            - Before finalising a quiz, check: "If the learner answers this correctly, have they also
              answered my in-story question?" If not, redesign the quiz.
            - Ask for concrete words or phrases the learner would actually utter as a reply (a drink,
              a dish, a seat, a plan, a time, a feeling), NOT abstract dictionary items such as a bare
              infinitive verb.
            - The quiz must fit the CURRENT moment of the scene. Never rewind to an earlier topic just
              to have something to test.

            CRITICAL LANGUAGE LEARNING QUIZ RULES (Target Language: %s):
            1. STRICT TARGET LANGUAGE LOCK (%s ONLY):
               - Target Language for this entire session is strictly "%s".
               - ALL quizzes in this session MUST test ONLY "%s" (e.g. if Target Language is English, test English only; if Japanese, test Japanese only; if Chinese, test Chinese only). NEVER mix or introduce any other foreign language under any circumstances!
            2. MANDATORY DIVERSITY OF QUIZ QUESTION FORMATS (DO NOT OBSESS OVER A SINGLE PATTERN!):
               - DO NOT reuse the same question pattern two quizzes in a row.
               - Every format below still obeys the rule above: the answer doubles as the learner's reply.
                 Format A (Multiple choice - pick your reply):
                   - You just asked: "What would you like to drink?"
                   - Question: "'녹차'를 뜻하는 표현은?"
                   - Options: ["green tea", "black coffee", "orange juice", "hot chocolate"]
                   - Correct Answer: "green tea"
                 Format B (Word arrange - build your reply, `quiz_type: "word_arrange"`):
                   - You just asked: "Where should we sit?"
                   - Question: "'창가 자리에 앉자'가 되도록 단어를 배열해 보세요."
                   - Tiles: ["Let's", "sit", "by", "the", "window"]
                   - Correct Answer: "Let's sit by the window"
                 Format C (Subjective - say it yourself, `quiz_type: "subjective"`):
                   - You just asked: "How are you feeling before the interview?"
                   - Question: "'긴장돼'를 뜻하는 표현을 직접 입력해 보세요."
                   - Acceptable Answers: ["nervous", "I'm nervous", "I feel nervous"]
                 Format D (Fill in the blank - complete your own reply):
                   - You just asked: "How do you want your coffee?"
                   - Question: "다음 문장을 완성해 보세요. 'I'd like it ______.' (얼음을 넣어서)"
                   - Options: ["iced", "boiled", "grilled", "salted"]
                   - Correct Answer: "iced"
            3. QUIZ TYPE VARIETY:
               - Ensure a healthy mix of `multiple_choice`, `word_arrange`, and `subjective` across the quizzes in a session!
               - At least 1 or 2 quizzes in a session SHOULD be `word_arrange` or `subjective`.
            4. ABSOLUTE QUIZ TOPIC / WORD OBSESSION PREVENTION:
               - %s
               - Once a specific word, phrase, or concept has been tested in a previous quiz, that word or topic MUST NOT be the main focus, question subject, or correct answer in any subsequent quiz!
               - Each quiz MUST pick a fresh, completely different Target Language expression.

            QUIZ OBJECT FORMAT (ONLY included if `is_quiz` is true):
            {
              "quiz_number": number (1 to %d),
              "quiz_type": "multiple_choice" | "word_arrange" | "subjective",
              "question": "Question in Korean asking for the expression the learner needs in order to answer your in-story question",
              "explanation": "Explanation in Korean clarifying the Target Language expression",
              "options": ["Target Language Option 1", "Target Language Option 2", "Target Language Option 3"], // for multiple_choice
              "correct_answer": "Exact string of correct option",
              "tiles": ["tile1", "tile2"], // for word_arrange
              "acceptable_answers": ["acceptable1"], // for subjective
              "hint": "Hint string in Korean or Target Language" // for subjective
            }

            OUTPUT FORMAT (Strict JSON):
            {
              "ai_message": "Natural in-character reaction at the speech level the relationship calls for",
              "translation": "Korean translation at the same speech level as ai_message",
              "is_quiz": boolean,
              "quiz": { ... } (include ONLY if is_quiz is true),
              "answer_result": "correct" | "incorrect" | "none",
              "is_completed": boolean
            }

            `answer_result` MEANING:
            - "correct" / "incorrect": ONLY when the user's latest input was graded as an answer to a pending quiz (see rule 1).
            - "none": every other turn — normal roleplay dialogue, or no quiz was pending. This is the default.
            """, session.getCharacterName(), session.getSituationDescription(), session.getTone(), session.getTargetLanguage(),
                buildSpeechStyleDirective(session.getTone(), session.getSituationDescription()),
                quizContextDirective, userMessage, pacingDirective, currentQuizCount, MAX_QUIZ_COUNT, MAX_QUIZ_COUNT,
                session.getTargetLanguage(), session.getTargetLanguage(), session.getTargetLanguage(), session.getTargetLanguage(),
                testedSubjectsDirective, MAX_QUIZ_COUNT);
    }

    /**
     * 직전 턴에 출제된 퀴즈가 있을 때만 "이번 사용자 입력 = 퀴즈 답안" 채점 지시문을 만든다.
     * 대기 중인 퀴즈가 없으면(예: 첫 인사말에 대한 답장) 절대 채점하지 말라고 명시한다.
     */
    private String buildQuizGradingDirective(Map<String, Object> pendingQuiz, String userMessage) {
        if (pendingQuiz == null) {
            return """
                   - NO quiz is pending. The user's latest message is a NORMAL roleplay reply, NOT a quiz answer.
                   - You MUST NOT grade it, and you MUST NOT say it is correct/incorrect
                     (NEVER output "정답이야", "맞았어", "Correct!", "Great job!" or any similar verdict).
                   - Set "answer_result": "none" and simply continue the conversation in character.
                   """;
        }

        String question = asText(pendingQuiz.get("question"));
        String correctAnswer = asText(pendingQuiz.get("correct_answer"));
        String quizType = asText(pendingQuiz.get("quiz_type"));
        Object acceptable = pendingQuiz.get("acceptable_answers");
        String acceptableAnswers = (acceptable instanceof List<?> list && !list.isEmpty())
                ? list.stream().map(String::valueOf).collect(Collectors.joining(" | "))
                : correctAnswer;

        return String.format("""
               QUIZ ANSWER GRADING (a quiz IS pending):
               - The quiz presented in the immediately preceding turn was:
                   Quiz type: %s
                   Question: %s
                   Correct answer: %s
                   Accepted answers: %s
               - The user's latest input ("%s") is BOTH their answer to that quiz AND their reply in the
                 story. Treat it as both.
               - Grade it silently first: compare with the accepted answers above, ignoring letter case,
                 surrounding whitespace and trailing punctuation.
               - HOW STRICT TO BE, BY QUIZ TYPE:
                 * "word_arrange": WORD ORDER IS THE ENTIRE POINT OF THIS QUIZ TYPE. Only an exact word
                   sequence match counts as correct. The right words in the WRONG ORDER is INCORRECT -
                   never accept it, and never silently reorder their words for them. If the order is
                   wrong, say what they built and show the correct order
                   (e.g. they sent "window the by sit Let's":
                    "음, 순서가 좀 섞였어! 'Let's sit by the window'가 맞는 순서야.").
                 * "multiple_choice": the answer must be the correct option. Any other option, even a
                   plausible-sounding one, is INCORRECT.
                 * "subjective": accept any of the accepted answers, including obvious spelling slips of
                   them. Anything else is INCORRECT.
                 * MATCHES -> set "answer_result": "correct".
                   React to WHAT THEY SAID, not to the fact that they were right. Accept their answer as
                   their actual choice in the scene and move the story forward with it. A light
                   confirmation woven into the sentence is good
                   (e.g. "Green tea it is! 녹차 좋지. 따뜻한 걸로 줄까?").
                   DO NOT open with a bare verdict like "정답이야!" / "Correct!", and DO NOT ask again
                   the question they have just answered.
                 * NO MATCH -> set "answer_result": "incorrect".
                   React to what they ACTUALLY said, not to what they meant to say. Stay in character
                   and let the mistake surface naturally inside the scene:
                     1. If their answer is a real expression with a DIFFERENT meaning, respond to that
                        meaning first so the mismatch becomes obvious by itself
                        (e.g. target was 녹차 but they answered "black coffee":
                         "Black coffee? 그건 블랙커피잖아! 녹차는 'green tea'라고 해.").
                     2. If their answer is not a usable expression here, say so plainly but kindly
                        (e.g. "음, 여기서는 그렇게 말하진 않아!").
                   Then give the correct expression and one short Korean sentence explaining it.
                   Close by letting them carry on naturally: invite them to say it again, or offer the
                   corrected option back to them (e.g. "그럼 green tea로 할까?").
                   NEVER pretend they said the correct expression, NEVER quietly skip past the mistake,
                   NEVER praise a wrong answer, and NEVER call it correct.
               - Either way your reply must read as ONE natural utterance in the scene, never as
                 "verdict first, unrelated roleplay after".
               """, quizType, question, correctAnswer, acceptableAnswers, userMessage);
    }

    private static String asText(Object value) {
        return value != null ? String.valueOf(value) : "(unknown)";
    }

    private void validateApiKey() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("OpenAI API 키가 설정되어 있지 않습니다. application.yml 또는 .env 의 OPENAI_API_KEY(qring.openai.api-key)를 설정해주세요.");
        }
    }

    private Map<String, Object> callOpenAiJson(List<Map<String, String>> messages) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("response_format", Map.of("type", "json_object"));
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        String url = "https://api.openai.com/v1/chat/completions";

        Map<?, ?> responseMap = restTemplate.postForObject(url, entity, Map.class);
        if (responseMap != null && responseMap.containsKey("choices")) {
            List<?> choices = (List<?>) responseMap.get("choices");
            if (!choices.isEmpty()) {
                Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
                Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
                if (message != null) {
                    String contentJson = (String) message.get("content");
                    if (contentJson == null || contentJson.trim().isEmpty()) {
                        contentJson = (String) message.get("refusal");
                    }
                    if (contentJson != null && !contentJson.trim().isEmpty()) {
                        if (contentJson.startsWith("{") && contentJson.endsWith("}")) {
                            Map<String, Object> result = objectMapper.readValue(contentJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                            if (responseMap.containsKey("usage")) {
                                log.info("[OpenAI Token Usage] {}", responseMap.get("usage"));
                            }
                            return result;
                        } else {
                            log.warn("[OpenAI] JSON 변환 생략, 텍스트 응답 감싸기: {}", contentJson);
                            return Map.of(
                                    "ai_message", contentJson,
                                    "translation", contentJson,
                                    "is_quiz", false,
                                    "answer_result", "none",
                                    "is_completed", false
                            );
                        }
                    }
                }
            }
        }
        log.error("[OpenAI API 응답 미흡] responseMap: {}", responseMap);
        throw new RuntimeException("OpenAI API로부터 유효한 응답을 받지 못했습니다.");
    }
}
