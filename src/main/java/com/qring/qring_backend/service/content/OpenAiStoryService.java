package com.qring.qring_backend.service.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qring.qring_backend.domain.content.StorySession;
import com.qring.qring_backend.dto.content.StoryStartRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiStoryService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiStoryService.class);

    @Value("${qring.openai.api-key:}")
    private String apiKey;

    @Value("${qring.openai.model:gpt-4o-mini}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 1단계: 대화 오프닝 생성 - 역할극 세션의 첫 인사를 OpenAI(ChatGPT)로 실시간 생성
     */
    public Map<String, Object> generateOpening(StoryStartRequest request, int levelCode) {
        validateApiKey();

        String systemPrompt = String.format("""
            You are AI Partner "%s", an adaptive roleplay partner in a language-learning app.
            Situation: %s
            Requested Mood/Tone: %s
            Target Language: %s (User Level Code: %d)

            CRITICAL DYNAMIC TONE & KOREAN TRANSLATION SPEECH STYLE RULES:
            - Requested Tone/Mood: "%s"
            - IF Requested Tone is "격식있게", "비즈니스", "정중하게", "Formal", "Business":
              1. Korean translation ("translation") MUST STRICTLY USE POLITE FORMAL HONORIFICS (존댓말: ~합니다, ~하세요, ~입니까?, ~입니다).
              2. Target language ("ai_message") MUST be polite, professional, and respectful.
            - IF Requested Tone is "다정하게", "친근하게", "친구", "Casual", "Friendly":
              1. Korean translation ("translation") MUST STRICTLY USE FRIENDLY CASUAL BANMAL (반말: ~해!, ~야?, ~하자, ~했어?, ~마실래?). NEVER use formal honorifics (존댓말: ~습니다, ~해요) when Tone is casual!
              2. Target language ("ai_message") MUST sound warm, informal, and close like a friend.
            - OTHER/DEFAULT: Match the persona relationship implied by Situation ("%s") and Tone ("%s").

            Generate the first opening message from you (the AI partner) to start the scenario naturally.
            You MUST return your response formatted strictly as a valid json object with the following fields:
            {
              "ai_message": "Opening line in target language or Korean as appropriate",
              "translation": "Korean translation matching the requested speech style"
            }
            """, request.getCharacterName(), request.getSituationDescription(), request.getTone(), request.getTargetLanguage(), levelCode, request.getTone(), request.getSituationDescription(), request.getTone());

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

    /**
     * 2단계: 턴 바이 턴 대화 - 사용자의 대답을 받아 OpenAI(ChatGPT)로 100% 실시간 대화 반응 및 퀴즈 생성
     */
    public Map<String, Object> generateTurnResponse(StorySession session, String userMessage) {
        validateApiKey();

        int currentQuizCount = session.getQuizCount();
        int turnsSinceLastQuiz = session.getTurnsSinceLastQuiz();
        boolean allowQuiz = turnsSinceLastQuiz >= 2 && currentQuizCount < 5;

        String pacingDirective = allowQuiz
                ? String.format("PACING RULE: Sufficient dialogue turns have passed (%d turns since last quiz). You SHOULD now present a relevant quiz moment matching the recent context by setting `is_quiz: true`.", turnsSinceLastQuiz)
                : String.format("STRICT PACING RULE: ONLY %d dialogue turn(s) passed since last quiz/start. YOU MUST SET `is_quiz: false` FOR THIS TURN! Do NOT output a quiz yet. Continue the natural dialogue (A-B-A-B dialogue turn) and ask an engaging follow-up question.", turnsSinceLastQuiz);

        String testedSubjectsDirective = session.getTestedQuizSubjects().isEmpty()
                ? "No quiz topics have been tested yet."
                : "FORBIDDEN ALREADY-TESTED QUIZ TOPICS/WORDS (NEVER TEST OR FOCUS ON ANY OF THESE AGAIN): " + String.join(", ", session.getTestedQuizSubjects());

        String quizContextDirective = (turnsSinceLastQuiz == 1)
                ? String.format("""
                  CRITICAL PREVIOUS-TURN QUIZ ANSWER RECOGNITION:
                  - A quiz was presented in the immediately preceding turn.
                  - The user's latest input ("%s") is their SUBMITTED QUIZ ANSWER to that previous quiz!
                  - DO NOT treat their quiz answer ("%s") as a direct response to your previous roleplay scenario question!
                  - In your response (`ai_message` / `translation`):
                    1. Briefly and naturally acknowledge/praise their quiz answer in character (e.g., "정답이야! 'dessert'가 맞아!", "Great job! '%s' is correct!").
                    2. Then seamlessly resume the ongoing roleplay scenario where it left off, and ask your next natural story question.
                  """, userMessage, userMessage, userMessage)
                : "";

        String systemPrompt = String.format("""
            You are AI Partner "%s", an adaptive conversation partner in a language-learning app.
            Situation: %s
            Requested Mood/Tone: %s
            Target Language: %s

            CRITICAL DYNAMIC TONE & KOREAN TRANSLATION SPEECH STYLE RULES:
            - Requested Tone/Mood: "%s"
            - IF Requested Tone is "격식있게", "비즈니스", "정중하게", "Formal", "Business":
              1. Korean translation ("translation") MUST STRICTLY USE POLITE FORMAL HONORIFICS (존댓말: ~합니다, ~하세요, ~입니까?, ~입니다).
              2. Target language ("ai_message") MUST be polite, professional, and respectful.
            - IF Requested Tone is "다정하게", "친근하게", "친구", "Casual", "Friendly":
              1. Korean translation ("translation") MUST STRICTLY USE FRIENDLY CASUAL BANMAL (반말: ~해!, ~야?, ~하자, ~했어?, ~마실래?). NEVER use formal honorifics (존댓말: ~습니다, ~해요) when Tone is casual!
              2. Target language ("ai_message") MUST sound warm, informal, and close like a friend.
            - OTHER/DEFAULT: Match the persona relationship implied by Situation ("%s") and Tone ("%s").

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
            7. Current Quiz Count Given So Far: %d / 5.
               - If `currentQuizCount == 5` and all 5 quizzes are finished, wrap up the scene with `is_completed: true`.

            CRITICAL LANGUAGE LEARNING QUIZ RULES (Target Language: %s):
            1. STRICT TARGET LANGUAGE LOCK (%s ONLY):
               - Target Language for this entire session is strictly "%s".
               - ALL 5 quizzes in this session MUST test ONLY "%s" (e.g. if Target Language is English, test English only; if Japanese, test Japanese only; if Chinese, test Chinese only). NEVER mix or introduce any other foreign language under any circumstances!
            2. MANDATORY DIVERSITY OF QUIZ QUESTION FORMATS (DO NOT OBSESS OVER A SINGLE PATTERN!):
               - DO NOT use the same repetitive question pattern (e.g. "방금 대화에서 'X'를 뜻하는 표현은?") for every quiz!
               - You MUST dynamically rotate and use diverse question formats matching real language test patterns, such as:
                 Format A (Sentence Fill-In-The-Blank):
                   - Question: "다음 문장의 빈칸에 들어갈 알맞은 표현은? 'Her eyes were red and ______.'"
                   - Options: ["watery", "swollen", "tired", "burning"]
                   - Correct Answer: "swollen"
                 Format B (Contextual Emotion / Situation Meaning):
                   - Question: "상대방이 긴장하거나 불안한 상태를 표현할 때 사용한 단어는?"
                   - Options: ["nervous", "calm", "sleepy", "excited"]
                   - Correct Answer: "nervous"
                 Format C (Idiom / Phrasal Verb & Word Meaning):
                   - Question: "대화에서 '포기하다'라는 의미로 쓰인 알맞은 숙어는?"
                   - Options: ["give up", "give off", "give away", "give out"]
                   - Correct Answer: "give up"
                 Format D (Word Arrange - `quiz_type: "word_arrange"`):
                   - Question: "방금 대화에서 나온 문장을 올바른 순서로 완성해 보세요."
                   - Tiles: ["Let's", "grab", "that", "window", "seat"]
                   - Correct Answer: "Let's grab that window seat"
                 Format E (Subjective Word Recall - `quiz_type: "subjective"`):
                   - Question: "'갑자기 의식을 잃고 쓰러지다'를 뜻하는 대화 속 숙어 표현은?"
                   - Acceptable Answers: ["passed out", "collapsed", "fainted"]
            3. QUIZ TYPE VARIETY:
               - Ensure a healthy mix of `multiple_choice`, `word_arrange`, and `subjective` across the 5 quizzes in a session!
               - At least 1 or 2 quizzes in a session SHOULD be `word_arrange` or `subjective`.
            4. ABSOLUTE QUIZ TOPIC / WORD OBSESSION PREVENTION:
               - %s
               - Once a specific word, phrase, or concept has been tested in a previous quiz, that word or topic MUST NOT be the main focus, question subject, or correct answer in any subsequent quiz!
               - Each quiz MUST pick a fresh, completely different Target Language expression from the latest conversation turns.

            QUIZ OBJECT FORMAT (ONLY included if `is_quiz` is true):
            {
              "quiz_number": number (1 to 5),
              "quiz_type": "multiple_choice" | "word_arrange" | "subjective",
              "question": "Question in Korean testing a NEW Target Language vocabulary or expression",
              "explanation": "Explanation in Korean clarifying the Target Language expression",
              "options": ["Target Language Option 1", "Target Language Option 2", "Target Language Option 3"], // for multiple_choice
              "correct_answer": "Exact string of correct option",
              "tiles": ["tile1", "tile2"], // for word_arrange
              "acceptable_answers": ["acceptable1"], // for subjective
              "hint": "Hint string in Korean or Target Language" // for subjective
            }

            OUTPUT FORMAT (Strict JSON):
            {
              "ai_message": "Natural AI reaction matching requested tone and relationship",
              "translation": "Korean translation strictly matching requested speech style (formal honorifics for business, casual banmal for friendly)",
              "is_quiz": boolean,
              "quiz": { ... } (include ONLY if is_quiz is true),
              "is_completed": boolean
            }
            """, session.getCharacterName(), session.getSituationDescription(), session.getTone(), session.getTargetLanguage(), session.getTone(), session.getSituationDescription(), session.getTone(), quizContextDirective, userMessage, pacingDirective, currentQuizCount, session.getTargetLanguage(), session.getTargetLanguage(), session.getTargetLanguage(), session.getTargetLanguage(), testedSubjectsDirective);

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
