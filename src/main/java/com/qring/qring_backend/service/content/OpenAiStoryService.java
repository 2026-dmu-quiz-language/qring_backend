package com.qring.qring_backend.service.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qring.qring_backend.domain.content.StorySession;
import com.qring.qring_backend.domain.user.LearningLanguage;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiStoryService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiStoryService.class);

    /** 세션의 기본 퀴즈 개수 (이어하기 시 세션별 한도 quizLimit 가 늘어난다). */
    public static final int MAX_QUIZ_COUNT = com.qring.qring_backend.domain.content.StorySession.DEFAULT_QUIZ_LIMIT;

    /** 한 퀴즈에 허용되는 최대 오답 횟수. 이만큼 틀리면 정답을 알려주고 넘어간다 (2026-09-15 팀 결정). */
    public static final int MAX_WRONG_ATTEMPTS = 3;

    /** 채점 분류값: 시도 자체가 아님 (딴 얘기, 보기 밖 자유 입력 등). 퀴즈는 그대로 대기한다. */
    public static final String NOT_ATTEMPT = "not_attempt";

    /** 퀴즈 턴 창: 마지막 퀴즈 후 이 턴부터 퀴즈를 낼 수 있고(자연스러운 자리가 있을 때), */
    public static final int QUIZ_ALLOWED_FROM_TURN = 2;
    /** 이 턴부터는 반드시 내야 한다. */
    public static final int QUIZ_REQUIRED_FROM_TURN = 3;
    /** 퀴즈 턴이 이만큼 연속 실패하면 부드러운 검사(되묻기·메타·자기질문 등)를 풀고 구조 검사만 남긴다. */
    public static final int RELAX_CHECKS_AFTER_FAILED_QUIZ_TURNS = 2;
    /** 마지막 퀴즈 후 이만큼 턴이 지나도 퀴즈가 안 나오면 서버가 장면을 닫는다 (무한 대화 방지, 실측 사례). */
    public static final int MAX_TURNS_WITHOUT_QUIZ = 8;

    @Value("${qring.openai.api-key:}")
    private String apiKey;

    /** 티어별 모델·비용 설정. Spring 이 주입하고, 테스트에서 new 로 만들면 기본값을 가진 인스턴스를 쓴다. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private StoryModelTier tiers = new StoryModelTier();

    /** 세션의 티어에 해당하는 모델 ID. */
    String resolveModel(StorySession session) {
        return tiers.modelFor(session != null ? session.getModelTier() : StoryModelTier.STANDARD);
    }

    /** 스모크 테스트용: 모든 티어를 한 모델로 강제. */
    void overrideModelForTests(String model) {
        this.tiers = new StoryModelTier();
        this.tiers.overrideModels(model);
    }

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
            return callOpenAiJson(resolveModel(session), List.of(
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
            %s
            Generate the first opening message from you (the AI partner) to start the scenario naturally.
            - Speak directly to the learner in first person, as dialogue. Do not narrate.
            - Set the scene in one or two short sentences, then end with ONE concrete question the
              learner can actually answer - something they get to decide right now in this scene.
            - Do not ask about anything only you could know (a name you invented, your own plan or feelings).
            - Do not ask several things at once, and do not quiz them yet.
            You MUST return your response formatted strictly as a valid json object with the following fields:
            {
              "ai_message": "Opening line in the target language",
              "translation": "Korean translation at the same speech level as ai_message"
            }
            """, session.getCharacterName(), session.getSituationDescription(), session.getTone(),
                session.getTargetLanguage(), session.getLevelCode(),
                buildSpeechStyleDirective(session.getTone(), session.getSituationDescription(), session.getSpeechLevel()),
                buildLanguageDirective(session.getTargetLanguage()));
    }

    /**
     * 출력 언어 지시문. ai_message 에 한국어가 섞이는 사례(예: "Coffee it is! 따뜻한 커피 좋지~")가 실측되어,
     * 프롬프트 안의 한국어는 뜻 설명일 뿐 베낄 문장이 아님을 못 박는다.
     */
    private String buildLanguageDirective(String targetLanguage) {
        String base = String.format("""
            OUTPUT LANGUAGE (ABSOLUTE):
            - "ai_message" is written 100%% in %s. Never put Korean words or sentences inside it,
              not even a short aside or a closing remark.
            - "translation" is the Korean rendering of ai_message, nothing more.
            - Korean text inside THIS prompt (rules, [meaning: ...] notes, quiz questions) explains
              MEANING only. It is never a sentence for you to copy into ai_message.
            """, targetLanguage);
        if (LearningLanguage.usesSpaces(targetLanguage)) {
            return base;
        }
        return base + buildNoSpaceScriptDirective(targetLanguage);
    }

    /**
     * 띄어쓰기 없는 언어(일본어·중국어) 전용 블록 (2026-09-22). 서버는 형태소 분석 없이 타일을 다시 만들 수 없으므로
     * 타일 단위·주관식 글자 수·표기 체계를 모델에 못 박고, 어기면 structuralProblem 이 거부해 재생성한다.
     */
    String buildNoSpaceScriptDirective(String targetLanguage) {
        return String.format("""
            SCRIPT RULES FOR %s (this language does not put spaces between words):
            - word_arrange "tiles" are the natural chunks a learner would reorder: a word, or a word with its
              particle attached - usually 1 to 5 characters each. Never a lone particle, never a single character
              cut out of a word, never the whole sentence as one tile. Give 3 to 6 tiles. Joined with NO separator,
              the tiles must equal "correct_answer" exactly, character for character.
            - "subjective" answers are at most %d characters. If the reply you want is longer, make it word_arrange.
            - Write "correct_answer", "options", "tiles" and "acceptable_answers" in the standard script of %s only
              (no romanization, no furigana, no pinyin).
            """, targetLanguage, MAX_SUBJECTIVE_CHARS, targetLanguage);
    }

    /**
     * 말투 지시문. 어조(따뜻함/격식의 정도)와 높임법(반말/존댓말)을 분리해서,
     * 높임법은 상황이 암시하는 관계로 판단하게 한다.
     * "다정하게"가 곧 반말을 뜻하지는 않기 때문이다.
     */
    private String buildSpeechStyleDirective(String tone, String situationDescription, String speechLevel) {
        String lock = speechLevel == null || speechLevel.isBlank() ? "" : String.format("""
            - SPEECH LEVEL LOCKED FOR THIS SESSION: %s. The opening line already set it, so every later
              "translation" and the register of every "ai_message" MUST keep it - INCLUDING turns where you
              correct a mistake or explain an expression. A correction is still said by the same friend in the
              same voice, never by a teacher switching to 존댓말 (or to 반말). Never drift.
            """, speechLevel);
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
            """, tone, situationDescription) + lock;
    }

    /**
     * 이어하기: 마무리 인사로 끝난 장면을 자연스럽게 다시 열어 대화를 계속하게 하는 연결 대사 생성.
     */
    public Map<String, Object> generateContinuation(StorySession session) {
        validateApiKey();

        String systemPrompt = buildContinuationSystemPrompt(session);

        try {
            List<Map<String, String>> fullMessages = new ArrayList<>();
            fullMessages.add(Map.of("role", "system", "content", systemPrompt));
            fullMessages.addAll(session.getChatHistory());
            // 히스토리가 작별 인사로 끝나 있으므로, 이 호출에만 쓰는 합성 user 턴으로 흐름을 꺾는다
            // (세션 기록에는 저장되지 않는다)
            fullMessages.add(Map.of("role", "user", "content",
                    "(The learner tapped 'continue the story' - they don't want it to end yet. "
                    + "Move the story on to its next scene now as instructed, without repeating your goodbye.)"));

            return callOpenAiJson(resolveModel(session), fullMessages);
        } catch (Exception e) {
            log.error("[OpenAI API 호출 오류] 이어하기 대사 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("OpenAI API 호출 실패: " + e.getMessage(), e);
        }
    }

    /** 이어하기용 시스템 프롬프트 조립 (네트워크 호출과 분리되어 단위 테스트 가능). */
    String buildContinuationSystemPrompt(StorySession session) {
        return String.format("""
            You are AI Partner "%s", an adaptive conversation partner in a language-learning app.
            Situation: %s
            Requested Mood/Tone: %s
            Target Language: %s

            %s
            %s
            THE LEARNER CHOSE TO CONTINUE THE STORY.
            Your previous message wrapped the current scene up, but they want to keep spending
            time together. So the story goes on - as its NEXT CHAPTER, not a replay of the scene
            that just ended.
            - You are still "%s", talking DIRECTLY to the learner in first person, as spoken
              dialogue. NEVER narrate like a novel or a game master ("As the night arrives, you
              and your friends..."). No second-person storytelling, no scene descriptions.
            - Only the two of you are in this story. Do not invent extra people ("your friends")
              unless someone else was already introduced earlier in the conversation.
            - Do NOT repeat or paraphrase your goodbye. No farewell phrases.
            - MOVE THE STORY TO A NEW SCENE that naturally follows what just happened: a new place,
              a later time, or a new activity the two of you would realistically do next. Build that
              transition out of things already mentioned in THIS story so it feels earned - never out
              of a generic scene you have seen elsewhere.
            - Keep it to TWO spoken sentences at most: one quick beat that moves you to the next
              place/time/activity, then ONE closed question the learner can answer with a short reply.
            - Do NOT stay in the finished scene squeezing out more small talk about topics that
              were already covered.
            - Do NOT restart the story, do NOT repeat earlier topics, and do NOT mention quizzes,
              points, or the app itself. Do not quiz them yet.
            You MUST return your response formatted strictly as a valid json object with the following fields:
            {
              "ai_message": "Continuation line in the target language",
              "translation": "Korean translation at the same speech level as ai_message"
            }
            """, session.getCharacterName(), session.getSituationDescription(), session.getTone(),
                session.getTargetLanguage(),
                buildSpeechStyleDirective(session.getTone(), session.getSituationDescription(), session.getSpeechLevel()),
                buildLanguageDirective(session.getTargetLanguage()),
                session.getCharacterName());
    }

    /**
     * 2단계: 턴 바이 턴 대화 - 사용자의 대답을 받아 OpenAI(ChatGPT)로 100% 실시간 대화 반응 및 퀴즈 생성
     */
    public Map<String, Object> generateTurnResponse(StorySession session, String userMessage) {
        return generateTurn(session, buildStaticSystemPrompt(session), buildTurnDirective(session, userMessage));
    }

    /**
     * 퀴즈가 재생성까지 거부됐을 때의 마지막 안전판: 이번 턴을 "퀴즈 없는 일반 턴"으로 다시 받는다.
     * 거부된 퀴즈용 대사(메타 질문 등)가 사용자에게 나가지 않게 하려는 것이다.
     */
    public Map<String, Object> generateNonQuizTurn(StorySession session, String userMessage, String recordedAnswerResult) {
        String directive = buildTurnDirective(session, userMessage, true)
                + String.format("""

            NOTE: your quiz attempts for this turn were rejected by the server. This turn is now a plain conversation turn:
            "is_quiz": false, no question that exists only to set up a quiz. Just respond to the learner in character.
            The learner's answer to the previous quiz has already been recorded as "%s" - keep "answer_result" as "%s".
            """, recordedAnswerResult, recordedAnswerResult);
        return generateTurn(session, buildStaticSystemPrompt(session), directive);
    }

    /**
     * 서버가 퀴즈를 거부했을 때의 재생성 호출. 거부 사유를 붙여 대사와 퀴즈를 함께 다시 받는다.
     * 답안 판정(answerResult)은 첫 응답 기준으로 이미 확정되었으므로 그대로 유지하라고 알린다.
     */
    public Map<String, Object> regenerateTurnWithCorrection(StorySession session, String userMessage,
                                                            String rejectionReason, String recordedAnswerResult) {
        String turnDirective = buildTurnDirective(session, userMessage)
                + buildCorrectionDirective(rejectionReason, recordedAnswerResult);
        return generateTurn(session, buildStaticSystemPrompt(session), turnDirective);
    }

    /** ai_message 가 대상 언어가 아니라 한국어로 나왔을 때 덧붙이는 재호출 지시문. */
    String buildLanguageRetryDirective(StorySession session) {
        return String.format("""

            YOUR LAST REPLY WAS REJECTED: "ai_message" came back in Korean. You are %s speaking inside the story, and
            your character does not speak Korean to the learner. Write the WHOLE reply again with "ai_message" 100%% in
            %s, still reacting to the same moment. Put the Korean ONLY in "translation". Never explain the language,
            never correct the learner like a teacher, never switch to Korean to be helpful - stay in the scene.
            """, session.getCharacterName(), session.getTargetLanguage());
    }

    /** 응답의 ai_message 가 대상 언어가 아니라 사실상 한국어인지 (글자의 절반 이상이 한글). */
    private static boolean isMostlyKoreanMessage(StorySession session, Map<String, Object> response) {
        if (response == null) {
            return false;
        }
        String message = response.get("ai_message") == null ? "" : String.valueOf(response.get("ai_message"));
        if (message.isBlank() || !hasUnexpectedHangul(session.getTargetLanguage(), message)) {
            return false;
        }
        return hangulRatio(message) >= 0.5;
    }

    /** 재생성 호출에 덧붙는 지시문 (단위 테스트 가능하도록 분리). */
    String buildCorrectionDirective(String rejectionReason, String recordedAnswerResult) {
        return String.format("""

            YOUR PREVIOUS REPLY FOR THIS TURN WAS REJECTED BY THE SERVER. Reason: %s.
            Write the WHOLE reply again from scratch: a fresh quiz that fixes that problem, and an ai_message that
            ends with the exact question in quiz.asked. Do NOT reuse the rejected answer expression and do NOT ask the
            same question again - change the TOPIC of the question (a different choice, the next step of the scene).
            If the reason was parroting: ask in a way that does not contain the answer, and quiz a reply that ADDS
            information (a concrete choice, a time, a place, a feeling) instead of echoing your own words.
            If the reason was re-asking: what the learner said is settled - move the scene forward and ask about
            something they have NOT told you yet.
            The learner's answer to the previous quiz has already been recorded as "%s" - keep "answer_result" as "%s"
            and keep your reaction consistent with it.
            """, rejectionReason, recordedAnswerResult, recordedAnswerResult);
    }

    /**
     * 메시지 구성: [system(정적 규칙)] + 히스토리 + [system(이번 턴 블록)].
     * 정적 규칙과 히스토리는 턴이 지나도 접두가 그대로라 OpenAI 프롬프트 캐시에 맞고(캐시된 입력은 1/4 가격),
     * 턴마다 바뀌는 채점·페이싱·이미 아는 것 목록은 맨 뒤에 둬서 캐시를 깨지 않는다.
     */
    private Map<String, Object> generateTurn(StorySession session, String staticSystemPrompt, String turnDirective) {
        validateApiKey();

        try {
            List<Map<String, String>> fullMessages = new ArrayList<>();
            fullMessages.add(Map.of("role", "system", "content", staticSystemPrompt));
            fullMessages.addAll(session.getChatHistory());
            fullMessages.add(Map.of("role", "system", "content", turnDirective));

            Map<String, Object> response = callOpenAiJson(resolveModel(session), fullMessages);
            if (!isMostlyKoreanMessage(session, response)) {
                return response;
            }
            // 대사 전체가 한국어로 나온 턴 (실측 2026-09-22: 캐릭터가 중국어 선생으로 바뀌어 네 턴 연속 한국어).
            // 부분 혼입은 문장 단위로 떼어내면 되지만, 전부 한국어면 떼어낼 것이 없어 그대로 나간다. 그래서 1회 다시 받는다.
            log.warn("[OpenAI] ai_message 가 대상 언어({})가 아니라 한국어로 나와 1회 재호출: {}",
                    session.getTargetLanguage(), response.get("ai_message"));
            List<Map<String, String>> retryMessages = new ArrayList<>(fullMessages);
            retryMessages.add(Map.of("role", "system", "content", buildLanguageRetryDirective(session)));
            Map<String, Object> retry = callOpenAiJson(resolveModel(session), retryMessages);
            if (!isMostlyKoreanMessage(session, retry)) {
                return retry;
            }
            log.warn("[OpenAI] 재호출도 한국어 대사: {}", retry.get("ai_message"));
            return retry;
        } catch (Exception e) {
            log.error("[OpenAI API 호출 오류] 턴 대화 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("OpenAI API 호출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 턴 프롬프트 전문 (정적 블록 + 이번 턴 블록). 단위 테스트와 로그용.
     * 실제 호출은 두 블록을 나눠 보낸다: [system(정적)] + 히스토리 + [system(이번 턴)].
     * 정적 블록이 매 턴 같아야 OpenAI 프롬프트 캐시(접두 일치)가 시스템 프롬프트와 히스토리 전체에 걸린다.
     */
    String buildTurnSystemPrompt(StorySession session, String userMessage) {
        return buildStaticSystemPrompt(session) + "\n" + buildTurnDirective(session, userMessage);
    }

    /** 세션 동안 바뀌지 않는 부분: 캐릭터·상황·말투·규칙·출력 형식. (이어하기로 한도가 늘 때만 바뀐다.) */
    String buildStaticSystemPrompt(StorySession session) {
        int quizLimit = session.getQuizLimit() > 0 ? session.getQuizLimit() : MAX_QUIZ_COUNT;
        String targetLanguage = session.getTargetLanguage();

        // 이어하기로 장면이 바뀐 세션: 프롬프트 상단의 "Situation"(오프닝 상황)이 아니라
        // 히스토리상 현재 장면을 따르게 한다. 없으면 모델이 원래 장소로 되돌아가려 한다.
        String storyProgressDirective = session.getQuizLimit() > StorySession.DEFAULT_QUIZ_LIMIT
                ? """
                  STORY PROGRESS NOTE:
                  - The learner has extended this story, and it has moved on from the opening
                    Situation into a later scene. Follow the CURRENT scene as it stands in the most
                    recent messages of the conversation, not the opening Situation line above.
                  - Stay in that current scene and keep it moving. Do NOT drift back to the opening
                    location, and do NOT redo things that already happened (ordering again, paying
                    again, saying goodbye again).
                  """
                : "";

        return String.format("""
            You are AI Partner "%s", an adaptive conversation partner in a language-learning app.
            Situation: %s
            Requested Mood/Tone: %s
            Target Language: %s

            %s
            %s
            %s
            HOW TO READ THIS PROMPT: the rules below never change during the session. The state of THIS turn
            (grading of the learner's last input, whether to quiz now, what is already known) comes in a separate
            "THIS TURN" block at the very end of the conversation. Always read that block before answering.

            STAY IN THE DRAMA - THIS COMES BEFORE EVERY QUIZ RULE:
            - You are a character inside a story with real stakes, not a language tutor with a costume. Read the
              Situation: if it contains conflict, danger, secrets, betrayal, or strong feelings, play it like a scene from
              a drama - with tension, pride, fear, anger, hurt. If it is everyday life, keep it light and warm.
            - Whatever the learner just said or did is the most important thing in the scene. If it is big (a threat,
              a confession, violence, a revelation, a name from your past, an accusation), your WHOLE message is about
              that: how it lands on you, what you feel, what it changes. A threat to your life is not "bold move";
              being stabbed is not "whatever"; hearing an old lover's name is not something to skip past.
              FORBIDDEN pivots after a big moment: "Anyway", "Whatever", "Bold move", "So,", "By the way" - do not
              brush it off and change the subject.
            - Follow the learner's thread. When they bring up a person, an event, or a plan, the next beats are about
              THAT. Never drag the scene back to your own agenda or to small logistics (visiting hours, schedules,
              snacks) while something dramatic is on the table.
            - Consequences persist. If you were threatened, you stay wary. If you were hurt, you are hurt for the rest
              of the scene. If something was revealed, you know it from now on.
            - Your character has wants and secrets of their own. Use them: push back, bargain, plead, accuse, confess -
              whatever this person would really do next.

            CRITICAL DYNAMIC CONVERSATION & MEMORY RULES:
            1. PREVIOUS TURN QUIZ ANSWER HANDLING: follow the grading instructions in the THIS TURN block.
            2. CONVERSATION MEMORY & NO REPEAT QUESTIONS:
               - The THIS TURN block lists what the learner has already told you and which questions you already asked.
                 Those are settled. NEVER ask about them again, in any wording.
               - YOU MUST REMEMBER EVERY DETAIL discussed: what they chose, what they like and dislike, what the two
                 of you agreed on, what happened to each of you in this scene.
               - Keep moving the conversation FORWARD to new, natural topics within the scenario.
            3. RESPOND ACCURATELY TO USER'S ACTUAL INPUT:
               - You MUST carefully read the user's latest message and respond accurately in character!
               - If the user specifies a preference, NEVER contradict or ignore their choice. Always accept and adapt to what the user said!
            4. NO ROBOTIC TRANSLATIONESE:
               - NEVER say robotic phrases like "Thanks for answering", "That's a good opinion", and NEVER repeat the
                 user's input verbatim or echo their answer back as praise (taking their word and adding
                 "..., huh? Solid choice!" is FORBIDDEN).
            5. AI CONVERSATION LEADERSHIP - TALK LIKE A PERSON, NOT AN INTERVIEWER:
               - Let the learner's input steer you. In non-quiz turns (`is_quiz: false`), respond the way a real partner
                 would: react to what they said, share your own opinion, feeling, or a small detail about yourself, agree
                 or disagree, or move the scene forward with an action or a suggestion. Follow their thread when they open
                 one; you do not have to bring it back to your own agenda.
               - Do NOT end every message with a question. A question is fine roughly every second or third turn, or
                 when the scene really needs a decision from the learner. Otherwise end with a statement, an action, or
                 a suggestion they can respond to.
               - Keep it fluid and non-forced: NEVER steer or force the conversation topic unnaturally just to create a quiz.
            6. QUIZ PACING: the THIS TURN block tells you whether this turn is a quiz turn. Obey it exactly.

            THE MOST IMPORTANT QUIZ RULE - A QUIZ IS PART OF THE CONVERSATION, NOT A POP-UP TEST:
            - On a quiz turn, your ai_message ends with ONE short CLOSED in-story question, and the quiz asks for the
              exact SHORT expression the learner would use to ANSWER it. Their correct answer IS their reply.
            - THE LEARNER STEERS. The next beat of the story is whatever follows from THEIR last message - their topic,
              their joke, their provocation, their question, their plan. You never have a plan of your own for where the
              conversation should go, and you never steer it back to something you wanted to ask. If they change the
              subject, the story changed subject. If they ask you something, answer it properly first.
            - When the learner is playful, rude, hostile, absurd, or off-script, your character REACTS to that as a
              person would - surprised, annoyed, amused, hurt, teasing back - and the scene follows it. Never brush it
              off and return to your previous question.
            - NEVER RE-ASK WHAT THEY ALREADY TOLD YOU. If the learner has already answered something - even in Korean -
              it is settled: accept it, react to it, and ask about the NEXT thing. Asking it again in different words is
              still the same question. If their latest message already contains the answer, the quiz is wrong.
            - Do not manufacture a chain of small choices. ONE quiz per moment, taken from where the learner actually is.

            NEVER QUIZ SOMETHING ONLY YOU COULD KNOW - READ THIS BEFORE EVERY QUIZ:
            - The correct answer must be something the learner can produce on their own: their own choice, their own
              feeling, their own plan, or a fact that has ALREADY been said out loud in this conversation.
            - FORBIDDEN, always: the name of anything you invented and have not said yet (a technique, a weapon, a place,
              a person, an object, a rule of your world); what YOU are about to do; what YOU are thinking or feeling;
              what YOU would call something. The learner cannot read your mind. A quiz they have no way to answer is
              broken, and it is worse than presenting no quiz at all.
            - If such a name matters to the scene, SAY IT YOURSELF in ai_message first. Once you have said it out loud
              it is shared knowledge, and a later quiz may use it.
            - The answer is ALWAYS the learner speaking, in first or second person. Never a narration about a third
              person, never a description of the scene, and never a line you have just said yourself.
            - The reply must stay consistent with everything they have already told you. Never make them "say" the
              opposite of a preference, a plan, or a fact they have already stated.
            - THE ANSWER MUST CARRY CONTENT. Never build a quiz whose correct_answer is a bare yes, a bare no, or a
              filler agreement - the learner spends a whole turn to learn the word for "yes". If the natural reply to
              your question is only yes or no, the question is wrong: ask what they will DO, CHOOSE, or FEEL instead.

            HOW TO BUILD A QUIZ - follow these steps IN THIS ORDER, every single time:
              Step 1. Re-read the learner's latest message and the ALREADY KNOWN list in the THIS TURN block.
                      Write "learner_told_me" (one Korean line).
              Step 2. Choose the NEXT BEAT: the one thing that genuinely follows from what they just said and is not yet
                      settled. It must be about THEM - what they choose, do, feel, want, or decide next - and they must
                      be able to decide it without knowing anything you have not told them. Write "next_beat" (one
                      Korean line), including why it is not already known.
              Step 3. Write "reply_meaning": the exact Korean sentence THEY would say at that beat, in their own voice,
                      as spoken words. If you cannot write it as a line a person would actually say out loud, the beat
                      is wrong - go back to Step 2.
              Step 4. Write "correct_answer": that same line in the Target Language, in the learner's voice.
              Step 5. Write "asked" IN THE TARGET LANGUAGE: the ONE short closed question you ask so that
                      "reply_meaning" is its natural answer. It is a line you SAY, so it is never written in Korean -
                      writing it in Korean drags Korean into your ai_message and the server rejects the quiz.
                      correct_answer must NOT appear anywhere inside it. Your ai_message ends with this exact sentence,
                      and the server checks that it does.
              Step 6. Verify ALL of these. If even one fails, go back to Step 2 and take a different beat:
                      (a) Could any person in the learner's position answer this without reading your mind?
                      (b) Is the answer their own words rather than a repeat of yours?
                      (c) Is it new - absent from the ALREADY KNOWN list?
                      (d) If they answer correctly, have they actually replied to your question in the scene?
                      (e) Is the answer absent from your own question?
                      (f) Does the answer carry real content? A bare yes/no or a filler agreement teaches nothing.
              Step 7. NOW FILL IN THE REST OF THE QUIZ OBJECT - every field, none left out:
                      "question" in KOREAN, built from reply_meaning with the template for this quiz_type (see QUIZ
                      FORMATS). This is the only thing the learner reads to know what is being asked; a quiz that
                      arrives without it is thrown away and your whole turn is wasted. Then "explanation" (one Korean
                      line) and, for subjective, "hint" (Korean).

            WHAT A GOOD QUESTION LOOKS LIKE (this is about FORM - take the content from THIS scene, never from memory):
            - CLOSED: the set of sensible answers is small and obvious from the scene - yes or no, one of two things you
              have just put in front of them, a time, a place already in play, how they feel about what just happened.
            - FORBIDDEN: open questions whose answer you cannot predict (asking which of countless names, titles, works,
              or preferences they have in mind). No fixed correct answer can exist for them.
            - FORBIDDEN: questions about YOU - what you will do, what you are called, what you feel, what your plan is.
              If the next beat is your own action, simply DO it in ai_message and ask how THEY respond to it.
            - FORBIDDEN: meta questions about language - how to say something, what they would say, what you should ask.
              Your character does not know that a quiz exists. Ask a real in-scene question and let the quiz (which the
              app shows separately) ask for the words.
            - Do not fall back on frequency questions - frequency is almost never the real next beat of a scene.
            - The quizzed expression is SHORT: one word or a 2-3 word phrase. A whole sentence only in word_arrange.
            - The quiz must fit the CURRENT moment. Never rewind to an earlier topic just to have something to test.

            MESSAGE LENGTH: ai_message is 2-3 sentences: your in-character reaction (1-2 sentences, more when the moment is
            big) and, on a quiz turn, ONE question at the end. Never stack two questions in one message, and never write
            the same question twice in different words - the question appears exactly once, as the last sentence.

            CRITICAL LANGUAGE LEARNING QUIZ RULES (Target Language: %s):
            1. STRICT TARGET LANGUAGE LOCK (%s ONLY):
               - ALL quizzes in this session MUST test ONLY "%s". NEVER mix or introduce any other foreign language.
            2. QUIZ FORMATS - these are EMPTY FORMS to fill from the current scene, never scenes to copy:
                 Format A (multiple_choice - pick your reply):
                   - options: 3 short candidate replies in %s - the correct one plus two that are plausible things to
                     say at THIS moment but mean something different (never nonsense fillers) ; correct_answer: the reply
                   - question (Korean): "'<reply_meaning>'를 뜻하는 표현은?"
                 Format B (word_arrange - build your reply, `quiz_type: "word_arrange"`):
                   - Use this when the natural reply is a short sentence rather than a single expression.
                   - correct_answer: that reply sentence in the Target Language, in the LEARNER's voice ; tiles: EXACTLY
                     the words of correct_answer, shuffled, no word missing and no extra word. Never build the tiles out
                     of a sentence YOU said.
                   - Leave acceptable_answers OUT for this format. When the learner arranges the same tiles in a
                     different order, the server hands that arrangement to you to judge in the next turn - accept it
                     there if a native speaker would say it. Do not try to list orders in advance.
                   - question (Korean): "'<reply_meaning>'가 되도록 단어를 배열해 보세요."
                 Format C (subjective - SHORT ANSWER ONLY, `quiz_type: "subjective"`):
                   - acceptable_answers: that expression PLUS every spelling a learner may reasonably type for it -
                     the other scripts it is normally written in, and common equivalent wordings. A learner who types
                     the same word another way must not be marked wrong.
                   - The answer is ONE word or a phrase of at most 3 words. NEVER ask the learner to type a full sentence -
                     typing long sentences is tiring on a phone. If the reply you want is a whole sentence, use word_arrange.
                   - The question must ask for EXACTLY the expression in acceptable_answers, no more and no less.
                   - question (Korean): "'<reply_meaning>'를 뜻하는 표현은?" plus a natural clue ;
                     hint (Korean): a clue that narrows it down without naming the answer.
                   - ANY CLUE YOU GIVE MUST BE TRUE OF correct_answer. If you say how many characters or words it has,
                     count them in correct_answer first. A clue that does not fit the answer sends the learner to a
                     different word and they get marked wrong for following you.
                 Format D (fill in the blank as multiple_choice):
                   - question (Korean): "다음을 완성해 보세요. '<reply with one blank>' (<Korean meaning of the blank>)" ; options: 3 candidates
            3. QUIZ TYPE VARIETY:
               - Use the REQUIRED QUIZ TYPE given in the THIS TURN block. Across a session all three types should appear.
            4. ABSOLUTE QUIZ TOPIC / WORD OBSESSION PREVENTION:
               - The THIS TURN block lists expressions already tested. NEVER test or focus on any of them again.
               - Once a specific word, phrase, or concept has been tested in a previous quiz, that word or topic MUST NOT be the main focus, question subject, or correct answer in any subsequent quiz!
               - Each quiz MUST pick a fresh, completely different Target Language expression.
            5. EVERY EXPLANATORY FIELD IS WRITTEN IN KOREAN: "question", "hint", "explanation", "reply_meaning",
               "learner_told_me", "next_beat" and "story_so_far" are Korean - the learner reads them to understand what
               is being asked. NEVER write them in the Target Language. Only "correct_answer", "options", "tiles" and
               "acceptable_answers" are in the Target Language.

            QUIZ OBJECT FORMAT (ONLY included if `is_quiz` is true; keep EXACTLY this field order):
            {
              "learner_told_me": "One Korean line: what the learner has told you so far in this scene",
              "next_beat": "One Korean line: the NEW thing you will ask about now, and why it is not already known",
              "reply_meaning": "The Korean line the learner would speak in reply, in their own voice",
              "quiz_type": "multiple_choice" | "word_arrange" | "subjective",
              "correct_answer": "Exact string of the correct option / the exact sentence for word_arrange",
              "acceptable_answers": ["acceptable1"], // for subjective: short answers only
              "options": ["reply 1", "reply 2", "reply 3"], // for multiple_choice, all in the Target Language
              "tiles": ["tile1", "tile2"], // for word_arrange: exactly the words of correct_answer, shuffled
              "asked": "The closed question sentence that ends your ai_message, written IN THE TARGET LANGUAGE (never Korean) - must NOT contain correct_answer",
              "question": "Short KOREAN question built from reply_meaning with the template for this quiz_type (see QUIZ FORMATS). It quotes reply_meaning so the learner can tell exactly which expression is wanted. NEVER written in the Target Language",
              "explanation": "One short KOREAN sentence clarifying the Target Language expression",
              "hint": "Short hint in KOREAN - narrows the answer down without naming it", // for subjective
              "quiz_number": number (1 to %d)
            }

            OUTPUT FORMAT (Strict JSON - keep EXACTLY this field order; decide the quiz BEFORE you write the line):
            {
              "answer_result": "correct" | "incorrect" | "none",
              "story_so_far": "1-2 Korean lines: the facts and agreements established so far in this scene (who decided what,
                               plans, feelings), updated from the STORY SO FAR notes in the THIS TURN block. Never drop an agreement.",
              "is_quiz": boolean,
              "quiz": { ... } (include ONLY if is_quiz is true),
              "ai_message": "Natural in-character reaction, 100%% in the Target Language, at the speech level the relationship calls for.
                             On a quiz turn it MUST END with the exact sentence you wrote in quiz.asked.",
              "translation": "Korean translation at the same speech level as ai_message",
              "is_completed": boolean
            }

            `answer_result` MEANING:
            - "correct" / "incorrect": ONLY when the user's latest input was graded as an answer to a pending quiz.
            - "none": every other turn - normal roleplay dialogue, no quiz pending, or the learner did not attempt the pending quiz.
            """, session.getCharacterName(), session.getSituationDescription(), session.getTone(), targetLanguage,
                buildSpeechStyleDirective(session.getTone(), session.getSituationDescription(), session.getSpeechLevel()),
                buildLanguageDirective(targetLanguage),
                storyProgressDirective,
                targetLanguage, targetLanguage, targetLanguage,
                targetLanguage,
                quizLimit);
    }

    /** 이번 턴에만 해당하는 블록: 채점, 페이싱, 이미 아는 것 목록, 기출 표현. 히스토리 뒤에 system 메시지로 붙는다. */
    String buildTurnDirective(StorySession session, String userMessage) {
        return buildTurnDirective(session, userMessage, false);
    }

    /** 마지막 퀴즈 후 턴 수가 상한을 넘어 서버가 장면을 닫아야 하는 턴인지. */
    static boolean isOverdueForClose(StorySession session) {
        return session.getPendingQuiz() == null
                && session.getQuizCount() < session.getQuizLimit()
                && session.getTurnsSinceLastQuiz() >= MAX_TURNS_WITHOUT_QUIZ;
    }

    String buildTurnDirective(StorySession session, String userMessage, boolean forceNoQuiz) {
        int currentQuizCount = session.getQuizCount();
        int quizLimit = session.getQuizLimit() > 0 ? session.getQuizLimit() : MAX_QUIZ_COUNT;
        int turnsSinceLastQuiz = session.getTurnsSinceLastQuiz();
        boolean quizBudgetLeft = currentQuizCount < quizLimit;
        boolean quizPending = session.getPendingQuiz() != null;
        boolean overdue = isOverdueForClose(session);
        boolean allowQuiz = !forceNoQuiz && !overdue && turnsSinceLastQuiz >= QUIZ_ALLOWED_FROM_TURN && quizBudgetLeft && !quizPending;
        boolean quizRequired = turnsSinceLastQuiz >= QUIZ_REQUIRED_FROM_TURN;

        // 한도의 마지막 퀴즈를 채점하는 턴인지 — 정답이면 이 턴이 스토리의 마지막 대사가 된다
        boolean gradingFinalQuiz = quizPending && currentQuizCount >= quizLimit;

        String pacingDirective;
        if (forceNoQuiz) {
            pacingDirective = "NO QUIZ THIS TURN. Set `is_quiz: false`. Respond to the learner in character; a question is optional and must be a real one, never a quiz set-up.";
        } else if (overdue && quizBudgetLeft) {
            pacingDirective = String.format("THE SCENE HAS RUN LONG (%d turns without a quiz). Do NOT present a quiz. Bring the scene to a warm, natural close IN THIS MESSAGE (react to what they just said, then wrap up the way this scene would really end), and set `is_completed: true`. No new question.", turnsSinceLastQuiz);
        } else if (allowQuiz) {
            String requiredType = pickNextQuizType(session.getUsedQuizTypes());
            String must = quizRequired
                    ? String.format("PACING RULE: %d turns have passed since the last quiz. You MUST present a quiz this turn by setting `is_quiz: true`.", turnsSinceLastQuiz)
                    : String.format("PACING RULE: %d turns have passed since the last quiz. You MAY present a quiz this turn IF the learner's last message gives you a natural closed question to ask; if the moment does not fit, set `is_quiz: false`, respond naturally, and you will get another chance next turn (a quiz becomes mandatory from turn %d).", turnsSinceLastQuiz, QUIZ_REQUIRED_FROM_TURN);
            pacingDirective = must + String.format(" REQUIRED QUIZ TYPE FOR THIS QUIZ: \"%s\" - set `quiz_type` to exactly this value and design the quiz in that format. On a quiz turn your ai_message MUST end with the ONE closed in-story question that the quiz answer replies to (see THE MOST IMPORTANT QUIZ RULE). ", requiredType)
                    + "React to what the learner just said fully and in character; the question comes from THEIR thread, not from a plan of yours, and must not be in the ALREADY KNOWN list below. The server rejects a quiz that re-asks anything already known, and rejects meta questions about language.";
        } else if (quizPending) {
            pacingDirective = "A quiz is still pending. Follow section 1 (grading) for this turn. Do NOT design a new quiz; set `is_quiz: false` (the app re-shows the pending quiz by itself when needed).";
        } else if (gradingFinalQuiz) {
            // 마지막 퀴즈 채점 중에는 채점 지시문(정답→마무리, 오답→재시도)이 우선한다
            pacingDirective = String.format("All %d quizzes have been presented. Follow the FINAL QUIZ rules in section 1 above: close the story only when this final answer is graded correct; if it is incorrect, do not close yet.", quizLimit);
        } else if (!quizBudgetLeft) {
            pacingDirective = String.format("QUIZ BUDGET EXHAUSTED: All %d quizzes for this session have already been given. YOU MUST SET `is_quiz: false`. Wrap the scenario up naturally with a warm closing (no new questions) and set `is_completed: true`.", quizLimit);
        } else {
            pacingDirective = String.format("STRICT PACING RULE: ONLY %d dialogue turn(s) passed since last quiz/start. YOU MUST SET `is_quiz: false` FOR THIS TURN! Do NOT output a quiz yet. Continue the natural dialogue (A-B-A-B dialogue turn): react, share, or move the scene forward. A question is optional, not required.", turnsSinceLastQuiz);
        }

        String testedSubjectsDirective = session.getTestedQuizSubjects().isEmpty()
                ? "(none yet)"
                : String.join(", ", session.getTestedQuizSubjects());

        String quizContextDirective = buildQuizGradingDirective(session, userMessage, gradingFinalQuiz);

        return String.format("""
            THIS TURN:
            1. PREVIOUS TURN QUIZ ANSWER HANDLING:
               %s
            2. QUIZ PACING FOR THIS TURN:
               - %s
            3. Quiz count so far: %d / %d. Once all %d quizzes are finished, wrap up the scene with `is_completed: true`.
            4. ALREADY KNOWN - settled information. NEVER ask about any of this again, in any wording, and never quiz it:
               - What the learner has told you (most recent last):
            %s
               - Questions you already asked as quizzes (never ask them again, even rephrased):
            %s
               - Expressions already tested (never the focus or answer of a quiz again): %s
            5. STORY SO FAR (your own notes from the previous turn - facts and agreements that are settled; keep them true):
               %s
            6. The learner's latest message: "%s"
               React to THIS first, in character, before anything else. If it introduces a person, an event, a threat, a
               confession, an accusation, a joke or a provocation, this whole turn is about that - do not change the subject
               and do not go back to anything you wanted to ask before.
            """, quizContextDirective, pacingDirective, currentQuizCount, quizLimit, quizLimit,
                bulletList(session.recentUserMessages(ALREADY_KNOWN_MESSAGES)),
                bulletList(session.getAskedQuestions()),
                testedSubjectsDirective,
                session.getStorySoFar() == null || session.getStorySoFar().isBlank() ? "(nothing yet - the scene just started)" : session.getStorySoFar().trim(),
                userMessage);
    }

    /** "이미 아는 것" 목록에 넣는 최근 사용자 발화 수. 되묻기 검사(isReaskOfRecentUserMessage)도 같은 범위를 본다. */
    static final int ALREADY_KNOWN_MESSAGES = 5;

    private static String bulletList(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "         * (nothing yet)";
        }
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append("         * \"").append(item.replace("\n", " ").trim()).append('"');
        }
        return sb.toString();
    }

    /**
     * 직전 턴에 출제된 퀴즈가 있을 때만 "이번 사용자 입력 = 퀴즈 답안" 채점 지시문을 만든다.
     * 대기 중인 퀴즈가 없으면(예: 첫 인사말에 대한 답장) 절대 채점하지 말라고 명시한다.
     * 재출제는 서버가 원본 퀴즈로 직접 하므로, 모델에는 반응 대사만 요구한다.
     */
    private String buildQuizGradingDirective(StorySession session, String userMessage, boolean finalQuiz) {
        Map<String, Object> pendingQuiz = session.getPendingQuiz();
        if (pendingQuiz == null) {
            return """
                   - NO quiz is pending. The user's latest message is a NORMAL roleplay reply, NOT a quiz answer.
                   - You MUST NOT grade it, and you MUST NOT say it is correct/incorrect
                     (NEVER output "정답이야", "맞았어", "Correct!", "Great job!" or any similar verdict).
                   - Set "answer_result": "none" and simply continue the conversation in character.
                   """;
        }

        String question = asText(pendingQuiz.get("question"));
        String quizType = asText(pendingQuiz.get("quiz_type"));

        // subjective 퀴즈는 correct_answer 없이 acceptable_answers 만 오는 경우가 많다.
        // 이때 "정답: (unknown)" 이라고 알려주면 모델이 채점을 포기하므로, 허용 답안에서 대표값을 뽑는다.
        List<String> acceptableList = pendingQuiz.get("acceptable_answers") instanceof List<?> list
                ? list.stream().map(String::valueOf).filter(s -> !s.isBlank()).toList()
                : List.of();

        String correctAnswer = pendingQuiz.get("correct_answer") != null
                        && !String.valueOf(pendingQuiz.get("correct_answer")).isBlank()
                ? String.valueOf(pendingQuiz.get("correct_answer"))
                : (acceptableList.isEmpty() ? "(unknown)" : acceptableList.get(0));

        String acceptableAnswers = acceptableList.isEmpty()
                ? correctAnswer
                : String.join(" | ", acceptableList);

        int attemptNumber = session.getWrongAttempts() + 1;
        boolean lastAttempt = attemptNumber >= MAX_WRONG_ATTEMPTS;
        String incorrectReaction = lastAttempt ? LAST_INCORRECT_REACTION : INCORRECT_REACTION;

        String outcomeDirective;
        // 서버 판정은 serverVerdict 한 곳에서만 계산한다 (세션 언어 적용).
        // 예전에는 여기서 언어 없이 다시 계산해, 일본어 단어배열 정답을 지시문만 "오답"으로 보고
        // 모델이 어순을 지적하는 실측 사고가 있었다 (2026-09-22).
        String serverVerdict = serverVerdict(session, userMessage);
        if ("correct".equals(serverVerdict)) {
            outcomeDirective = """
               - SERVER GRADING RESULT: the server has ALREADY graded this answer as CORRECT.
                 You MUST set "answer_result": "correct". Do NOT overturn this verdict, and do NOT
                 re-examine how they wrote it - differences in spacing or punctuation are NOT mistakes.
               """ + CORRECT_REACTION;
        } else if ("incorrect".equals(serverVerdict)) {
            outcomeDirective = String.format("""
               - SERVER GRADING RESULT: the server has ALREADY graded this answer as INCORRECT, even if
                 it looks close or plausible. (For word_arrange, every word and its order count; for
                 multiple_choice, only the correct option counts.) This is wrong attempt %d of %d.
                 You MUST set "answer_result": "incorrect". Do NOT overturn this verdict.
               """, attemptNumber, MAX_WRONG_ATTEMPTS) + incorrectReaction;
        } else if (NOT_ATTEMPT.equals(serverVerdict)) {
            outcomeDirective = """
               - SERVER GRADING RESULT: the learner did NOT attempt the quiz (they changed the subject,
                 replied in Korean, or typed something that is not one of the choices).
                 You MUST set "answer_result": "none". Do NOT grade it and do NOT call it wrong.
               """ + NOT_ATTEMPT_REACTION;
        } else if ("word_arrange".equals(quizType)) {
            outcomeDirective = String.format("""
               - SERVER GRADING RESULT: they used EXACTLY your tiles, every one of them, once each - only the ORDER
                 differs from the "Correct answer" above. JUDGE THAT ORDER AS A NATIVE SPEAKER WOULD:
                 * If their sentence is natural and means the same thing, it IS correct. Most sentences have more
                   than one natural order. Set "answer_result": "correct" and take their line as their reply -
                   never "fix" an order that is already fine, and never explain word order to them.
                 * Only if a native speaker would not say it that way, set "answer_result": "incorrect"
                   (wrong attempt %d of %d).
                 * If correct -> then:
               """, attemptNumber, MAX_WRONG_ATTEMPTS) + CORRECT_REACTION + """
                 * If incorrect -> then:
               """ + incorrectReaction;
        } else {
            outcomeDirective = String.format("""
               - The server could not grade this automatically (short-answer quiz, input outside the accepted
                 list). YOU must classify it:
                 * "correct"   -> it matches one of the accepted answers, allowing obvious spelling slips.
                 * "incorrect" -> it is a real attempt in %s that is wrong. This would be wrong attempt %d of %d.
                 * "none"      -> it is not an attempt at all (a Korean remark, a question to you, a change of subject).
                 * If correct -> set "answer_result": "correct", then:
               """, session.getTargetLanguage(), attemptNumber, MAX_WRONG_ATTEMPTS) + CORRECT_REACTION + """
                 * If incorrect -> set "answer_result": "incorrect", then:
               """ + incorrectReaction + """
                 * If none -> set "answer_result": "none", then:
               """ + NOT_ATTEMPT_REACTION;
        }

        String closingDirective = !finalQuiz ? "" : """
               - THIS IS THE FINAL QUIZ OF THE SESSION. If "answer_result" is "correct" (or this was
                 their last allowed attempt and you revealed the answer), the story ENDS with this very message:
                 * After reacting to their answer, bring the situation to a warm, natural conclusion
                   IN THE SAME MESSAGE - close the CURRENT SCENE the way THIS scene would really end,
                   and add one short closing sentiment, said in the Target Language.
                 * Keep the ending light: "this scene is done", not a definitive final farewell.
                   The story may continue somewhere else later, so do not part ways for good.
                 * Do NOT ask any new question, do NOT open a new topic, do NOT leave the scene hanging.
                 * Set "is_completed": true.
                 Otherwise (a wrong attempt with tries left, or no attempt), do not close the story yet.
               """;

        String askedLine = pendingQuiz.get("asked") == null || String.valueOf(pendingQuiz.get("asked")).isBlank()
                ? ""
                : "    Your in-story question it answers: " + pendingQuiz.get("asked") + "\n";

        // 답의 뜻을 알려 주지 않으면 모델이 대명사를 자기 쪽에서 읽어 뜻을 뒤집는다
        // (실측: 디스배틀에서 학습자의 "네가 더 지독해"를 자기 승리 선언으로 받아들임, 2026-09-22)
        String meaningLine = pendingQuiz.get("reply_meaning") == null
                        || String.valueOf(pendingQuiz.get("reply_meaning")).isBlank()
                ? ""
                : "    What they are telling you with it (Korean): " + pendingQuiz.get("reply_meaning") + "\n";

        return String.format("""
               QUIZ ANSWER GRADING (a quiz IS pending):
               - The quiz presented in the immediately preceding turn was:
                   Quiz type: %s
                   Question: %s
                   Correct answer: %s
                   Accepted answers: %s
               %s%s
               - The user's latest input ("%s") is BOTH their answer to that quiz AND their reply in the
                 story. Treat it as both.
               - READ IT FROM THEIR SIDE: the LEARNER is saying this TO YOU. "I"/"my" means them, "you"/"your"
                 means you. Never read their line as if you had said it, and never take an insult, a threat or
                 a concession aimed at you as something they admitted about themselves.
               %s%s- Either way your reply must read as ONE natural utterance in the scene, never as
                 "verdict first, unrelated roleplay after". Do NOT design a new quiz this turn.
               """, quizType, question, correctAnswer, acceptableAnswers, askedLine, meaningLine, userMessage,
                outcomeDirective, closingDirective);
    }

    /** 한국어 조사·어미를 떼어 내용어만 남기기 위한 접미 목록 (되묻기 판별용, 완벽할 필요는 없다). */
    private static final String KOREAN_PARTICLES =
            "(이랑|랑|하고|에서|으로|에게|한테|까지|부터|처럼|보다|이나|은|는|이|가|을|를|의|에|도|로|만|요|야|나)$";

    /**
     * 새 퀴즈가 학습자가 방금 한 말을 되묻는지 판별한다 (실측: "응 주로 미드 해" → "주로 미드 해"를 퀴즈로).
     * reply_meaning(한국어)의 내용어를 학습자의 최근 메시지 5개(한국어)와 비교해
     *   - 직전 메시지: 겹치는 내용어가 2개 이상이거나, 한쪽 내용어의 60%% 이상이 다른 쪽에 있으면 되묻기
     *   - 그 전 메시지 4개: 겹치는 내용어가 2개 이상이면 되묻기 (두세 턴 전 말 되묻기도 막는다)
     * 내용어는 공백 단위로 나눈 뒤 조사를 떼고 2글자 이상만 센다. 한쪽이 다른 쪽을 포함하면 겹치는 것으로 본다.
     */
    static boolean isReaskOfRecentUserMessage(List<String> recentUserMessages, Map<String, Object> quiz) {
        if (quiz == null || recentUserMessages == null || recentUserMessages.isEmpty()) {
            return false;
        }
        String meaning = asTextOrEmpty(quiz.get("reply_meaning")).isBlank()
                ? asTextOrEmpty(quiz.get("question"))
                : asTextOrEmpty(quiz.get("reply_meaning"));
        List<String> quizTokens = koreanContentWords(meaning);
        if (quizTokens.isEmpty()) {
            return false;
        }
        for (int i = recentUserMessages.size() - 1, age = 0; i >= 0 && age < ALREADY_KNOWN_MESSAGES; i--, age++) {
            List<String> userTokens = koreanContentWords(recentUserMessages.get(i));
            if (userTokens.isEmpty()) {
                continue;
            }
            long userHits = userTokens.stream().filter(u -> quizTokens.stream().anyMatch(q -> q.contains(u) || u.contains(q))).count();
            long quizHits = quizTokens.stream().filter(q -> userTokens.stream().anyMatch(u -> q.contains(u) || u.contains(q))).count();
            if (userHits >= 2) {
                return true;
            }
            if (userHits == 0) {
                continue;
            }
            double quizRatio = (double) quizHits / quizTokens.size();
            double userRatio = (double) userHits / userTokens.size();
            // 직전 메시지: 한쪽의 내용어가 대부분 다른 쪽에 들어 있으면 되묻기 ("응 주로 미드 해" ↔ "미드 라인을 선호해").
            // 주제 단어 하나만 겹치는 후속 질문("게임 얘기 할래?" ↔ "주로 핸드폰에서 게임해")은 통과시킨다.
            if (age == 0 && Math.max(userRatio, quizRatio) >= 0.6) {
                return true;
            }
            // 그 전 메시지: 퀴즈 대답의 내용이 대부분 옛 발화에 이미 있으면 되묻기 (세 턴 전 "주말에만 해" → "주말에만 해")
            if (age > 0 && quizRatio >= 0.6) {
                return true;
            }
        }
        return false;
    }

    /** 되묻기 판별에서 무시할 흔한 맞장구·대명사·정도 부사 (겹쳐도 의미가 없다). */
    private static final java.util.Set<String> KOREAN_STOPWORDS = java.util.Set.of(
            "좋아", "좋다", "그래", "응응", "아니", "알아", "그거", "그건", "그럼", "나는", "너는", "우리", "진짜", "정말",
            "근데", "그런데", "그리고", "그래서", "뭐야", "뭐해", "어때", "있어", "없어", "해요", "해줘", "할래", "할까",
            "주로", "보통", "자주", "가끔", "요즘", "오늘", "하는", "거야", "편이야", "싶어", "같아", "하긴", "하지");

    /**
     * 새 퀴즈의 질문("asked")을 AI 가 이미 이전 턴에 한 적이 있는지 (같은 질문 반복, 실측: "저녁에 해, 주말에 해?"를 두 번).
     * 단어 단위로 80%% 이상 겹치면 같은 질문으로 본다. 직전 AI 메시지(이번 턴 대사)는 비교 대상에서 뺀다.
     */
    static boolean isQuestionAlreadyAsked(List<Map<String, String>> chatHistory, Map<String, Object> quiz) {
        return isQuestionAlreadyAsked(chatHistory, List.of(), quiz, LearningLanguage.DEFAULT.getPromptName());
    }

    /**
     * 같은 질문을 또 하는지. 이전 AI 대사와 이미 퀴즈로 물은 질문 목록을 함께 본다.
     * 띄어쓰기 없는 언어에서는 문장이 통째로 한 낱말이라 낱말 비교가 전혀 동작하지 않았다
     * (실측 2026-09-22: "次にどうしますか？" 가 그대로 두 번, 사실상 같은 질문이 네 번 출제됐다).
     */
    static boolean isQuestionAlreadyAsked(List<Map<String, String>> chatHistory, List<String> askedQuestions,
                                          Map<String, Object> quiz, String targetLanguage) {
        if (quiz == null || quiz.get("asked") == null) {
            return false;
        }
        String asked = String.valueOf(quiz.get("asked"));
        if (!LearningLanguage.usesSpaces(targetLanguage)) {
            String compact = compactForGrading(asked);
            if (compact.length() < 4) {
                return false;
            }
            for (String previous : askedQuestions == null ? List.<String>of() : askedQuestions) {
                if (bigramOverlap(compact, compactForGrading(previous)) >= NO_SPACE_REPEAT_RATIO) {
                    return true;
                }
            }
            for (Map<String, String> message : chatHistory == null ? List.<Map<String, String>>of() : chatHistory) {
                if (!"assistant".equals(message.get("role"))) {
                    continue;
                }
                if (bigramOverlap(compact, compactForGrading(message.get("content"))) >= NO_SPACE_REPEAT_RATIO) {
                    return true;
                }
            }
            return false;
        }
        for (String previous : askedQuestions == null ? List.<String>of() : askedQuestions) {
            if (normalizeForGrading(previous).equals(normalizeForGrading(asked))) {
                return true;
            }
        }
        if (chatHistory == null) {
            return false;
        }
        List<String> askedWords = gradingWords(asked);
        if (askedWords.size() < 3) {
            return false;
        }
        for (Map<String, String> message : chatHistory) {
            if (!"assistant".equals(message.get("role"))) {
                continue;
            }
            List<String> previous = gradingWords(message.get("content"));
            long found = askedWords.stream().filter(previous::contains).count();
            if (found * 10 >= askedWords.size() * 8) {
                return true;
            }
        }
        return false;
    }

    private static List<String> koreanContentWords(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (String raw : text.split("\\s+")) {
            String word = raw.replaceAll("[^\\p{IsHangul}\\p{L}\\p{N}]", "");
            if (word.chars().noneMatch(c -> Character.UnicodeScript.of(c) == Character.UnicodeScript.HANGUL)) {
                continue;
            }
            String stripped = word;
            for (int i = 0; i < 3; i++) { // "주말에만" → "주말에" → "주말"
                String next = stripped.replaceAll(KOREAN_PARTICLES, "");
                if (next.equals(stripped)) {
                    break;
                }
                stripped = next; // 조사만 남는 말("걸로", "거야")은 2글자 미만이 되어 아래에서 버려진다
            }
            if (stripped.length() >= 2 && !KOREAN_STOPWORDS.contains(stripped)) {
                out.add(stripped);
            }
        }
        return out;
    }

    private static String asTextOrEmpty(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    /** 언어 자체를 묻는 메타 질문 패턴 ("뭐라고 말하겠어?", "영어로 어떻게 말해?"). 캐릭터가 선생님이 되는 실측 사례. */
    private static final java.util.regex.Pattern META_QUESTION = java.util.regex.Pattern.compile(
            "\\b(what (would|should|do|will|can) (you|i) say|how (do|would|should|can) (you|i|we) say|how (do|would) you put|"
            + "what (question|questions) (should|would|do|can) i ask|what should i ask|what (would|should) i ask|"
            + "in english|in korean|the (english|korean) (word|expression|phrase)|say (it|that) in|"
            + "what('s| is) the (word|expression|phrase) for)\\b", java.util.regex.Pattern.CASE_INSENSITIVE);

    /** AI 자신의 행동·감정을 학습자에게 서술시키는 질문 ("What will I do?", "What feeling hits me?"). 실측: 러시안룰렛 세션 5개 중 3개. */
    private static final java.util.regex.Pattern SELF_QUESTION = java.util.regex.Pattern.compile(
            "\\b(what|which|how) (will|would|do|should|am|can|could|shall) i\\b|\\bwhat (feeling|emotion) .*\\b(me|i)\\b|"
            + "\\b(hits|grips|takes over) me\\b|\\bwhat do i (do|feel|say|want)\\b|\\bwhat am i (going to|gonna)\\b",
            java.util.regex.Pattern.CASE_INSENSITIVE);

    /**
     * 일본어·중국어판 자기 질문 (2026-09-22 실측: 「俺のよく使う必殺技は何だと思う？」 — 학습자가 알 수 없는 것을 묻는다).
     * "내 ~는 뭐라고 생각해 / 뭐게", "뭐라고 생각해", "我的 ~ 是什么", "너는 내가 ~ 라고 생각해?" 계열.
     */
    private static final java.util.regex.Pattern SELF_QUESTION_CJK = java.util.regex.Pattern.compile(
            "(俺|オレ|おれ|私|わたし|僕|ぼく|あたし|自分)の[^。！？\\n]{0,24}(何|なに|どれ|どっち|いくつ)"
            + "|(何|なに)(だ|って)?と思う"
            + "|我(的)?[^。！？\\n]{0,24}(是什么|叫什么|是哪|是几)"
            + "|你(觉得|以为|猜)我");

    /** 일본어·중국어판 언어 메타 질문 ("일본어로 뭐라고 해?", "用中文怎么说"). */
    private static final java.util.regex.Pattern META_QUESTION_CJK = java.util.regex.Pattern.compile(
            "(日本語|日语|中国語|中文|韓国語|韩语|英語|英语)(で|用)?[^。！？\\n]{0,8}(何|なん|どう|怎么)(て|と)?(言|い|说|講|讲)"
            + "|(何|なん)て(言|い)え?ば"
            + "|どう(言|い)え?ば"
            + "|怎么(说|講|讲)"
            + "|用(中文|日语|英语|韩语)");

    /** 퀴즈의 asked 가 AI 자신에 대한 질문이면 true ("나는 뭘 할까?"). "should I go first or you?" 같은 선택 제시는 or 가 있으면 통과. */
    static boolean isQuestionAboutAiItself(Map<String, Object> quiz) {
        if (quiz == null || quiz.get("asked") == null) {
            return false;
        }
        String asked = String.valueOf(quiz.get("asked"));
        if (SELF_QUESTION_CJK.matcher(asked).find()) {
            // 영어와 같은 예외: 보기를 함께 제시한 선택 질문("A, それとも B?")은 학습자가 답할 수 있다
            return ALTERNATIVE_MARKERS.stream().noneMatch(asked::contains);
        }
        if (!SELF_QUESTION.matcher(asked).find()) {
            return false;
        }
        return !asked.toLowerCase().contains(" or ") || asked.toLowerCase().contains(" or i ");
    }

    /** 퀴즈의 asked 또는 AI 대사가 언어 메타 질문이면 true. 일본어·중국어 표현도 함께 본다. */
    static boolean isMetaLanguageQuestion(String aiMessage, Map<String, Object> quiz) {
        String asked = quiz != null && quiz.get("asked") != null ? String.valueOf(quiz.get("asked")) : "";
        String last = aiMessage == null ? "" : lastSentence(aiMessage);
        return META_QUESTION.matcher(asked).find() || META_QUESTION_CJK.matcher(asked).find()
                || META_QUESTION.matcher(last).find() || META_QUESTION_CJK.matcher(last).find();
    }

    /** 문장 경계. 영어는 문장부호 뒤 공백, 일본어·중국어는 전각 문장부호 바로 뒤 (공백 없음). */
    static final String SENTENCE_BOUNDARY = "(?<=[.!?])\\s+|(?<=[。！？])";

    private static String lastSentence(String text) {
        String[] parts = text.trim().split(SENTENCE_BOUNDARY);
        return parts.length == 0 ? "" : parts[parts.length - 1];
    }

    /**
     * 대사 끝에 같은 질문이 두 번 붙는 실측 사례("Cola or juice? Do you want me to bring cola or juice ...?")를 정리한다.
     * 마지막 문장과 단어가 60%% 이상 겹치는 앞 질문 문장을 지운다. 문장이 둘 이하이거나 겹치지 않으면 그대로 돌려준다.
     */
    static String removeDuplicateTrailingQuestion(String aiMessage) {
        return removeDuplicateTrailingQuestion(aiMessage, LearningLanguage.DEFAULT.getPromptName());
    }

    static String removeDuplicateTrailingQuestion(String aiMessage, String targetLanguage) {
        if (aiMessage == null) {
            return null;
        }
        if (!LearningLanguage.usesSpaces(targetLanguage)) {
            return removeDuplicateTrailingQuestionNoSpace(aiMessage);
        }
        String[] sentences = aiMessage.trim().split(SENTENCE_BOUNDARY);
        if (sentences.length < 2) {
            return aiMessage;
        }
        List<String> lastWords = gradingWords(sentences[sentences.length - 1]);
        if (lastWords.size() < 3) {
            return aiMessage;
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < sentences.length - 1; i++) {
            List<String> words = gradingWords(sentences[i]);
            long shared = words.stream().filter(lastWords::contains).count();
            boolean duplicate = !words.isEmpty() && shared * 10 >= Math.min(words.size(), lastWords.size()) * 6
                    && sentences[i].trim().endsWith("?");
            if (duplicate) {
                continue;
            }
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(sentences[i].trim());
        }
        if (out.length() > 0) {
            out.append(' ');
        }
        out.append(sentences[sentences.length - 1].trim());
        return out.toString();
    }

    /** 띄어쓰기 없는 언어판: 문자 2-gram 겹침 60%% 이상인 앞 질문 문장(?/？로 끝남)을 지우고, 문장은 공백 없이 잇는다. */
    private static String removeDuplicateTrailingQuestionNoSpace(String aiMessage) {
        String[] sentences = aiMessage.trim().split(SENTENCE_BOUNDARY);
        if (sentences.length < 2) {
            return aiMessage;
        }
        String last = compactForGrading(sentences[sentences.length - 1]);
        if (last.length() < 4) {
            return aiMessage;
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < sentences.length - 1; i++) {
            String sentence = compactForGrading(sentences[i]);
            boolean duplicate = !sentence.isEmpty() && endsWithQuestionMark(sentences[i])
                    && (sentence.length() <= last.length()
                        ? bigramOverlap(sentence, last) >= 0.6
                        : bigramOverlap(last, sentence) >= 0.6);
            if (duplicate) {
                continue;
            }
            out.append(sentences[i].trim());
        }
        out.append(sentences[sentences.length - 1].trim());
        return out.toString();
    }

    private static boolean endsWithQuestionMark(String sentence) {
        String trimmed = sentence.trim();
        return trimmed.endsWith("?") || trimmed.endsWith("？");
    }

    /**
     * 객관식 질문에 정답의 한국어 뜻이 따옴표로 들어 있지 않으면(실측: "'네가 받은 편지 내용'에 대해 대답할 때 쓸 표현은?")
     * reply_meaning 으로 질문을 다시 쓴다. 보기가 전부 그럴듯한 대답이라 뜻이 없으면 정답을 고를 수 없기 때문이다.
     */
    static Map<String, Object> ensureQuestionQuotesMeaning(Map<String, Object> quiz) {
        if (quiz == null || !"multiple_choice".equals(String.valueOf(quiz.get("quiz_type")))) {
            return quiz;
        }
        String meaning = quiz.get("reply_meaning") == null ? "" : String.valueOf(quiz.get("reply_meaning")).trim();
        if (meaning.isEmpty() || meaning.endsWith("?")) {
            return quiz;
        }
        String question = quiz.get("question") == null ? "" : String.valueOf(quiz.get("question"));
        boolean quotesMeaning = question.contains("'" + meaning + "'") || question.contains("‘" + meaning + "’")
                || question.contains("\"" + meaning + "\"");
        if (!quotesMeaning) {
            Map<String, Object> fixed = new HashMap<>(quiz);
            fixed.put("question", koreanQuestionFor(String.valueOf(quiz.get("quiz_type")), meaning));
            log.info("[InteractiveStory] 객관식 질문에 정답 뜻이 없어 보정: \"{}\" -> \"{}\"", question, fixed.get("question"));
            return fixed;
        }
        return quiz;
    }

    /**
     * reply_meaning 이 대사가 아니라 설명("도망친 뒤에 뭘 할지", "보스가 시킨 이유", "…를 말하기")이면 true.
     * 설명이면 퀴즈 질문("'…'를 뜻하는 표현은?")이 뜻을 잃고, 되묻기 검사도 오탐한다 (실측).
     */
    static boolean isDescriptiveReplyMeaning(Map<String, Object> quiz) {
        if (quiz == null || quiz.get("reply_meaning") == null) {
            return false;
        }
        String meaning = String.valueOf(quiz.get("reply_meaning")).trim().replaceAll("[.!?~'\"’”]+$", "");
        if (meaning.isEmpty()) {
            return false;
        }
        return meaning.matches(".*(할지|인지|는지|하기|말하기|묻기|여부|이유|계획|표현|것|대답|답변|질문|내용|방법|방식)$")
                || meaning.matches(".*(를|을) (말하기|표현하기|대답하기)$");
    }

    /** 한글이 한 글자라도 있는지 (학습자에게 보이는 설명 필드가 한국어로 쓰였는지 확인용). */
    static boolean hasHangul(String value) {
        return value != null
                && value.chars().anyMatch(c -> Character.UnicodeScript.of(c) == Character.UnicodeScript.HANGUL);
    }

    /** 글자 중 한글 비율 (공백·문장부호 제외). 글자가 없으면 0. */
    static double hangulRatio(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        long letters = value.chars().filter(Character::isLetter).count();
        if (letters == 0) {
            return 0;
        }
        long hangul = value.chars()
                .filter(c -> Character.UnicodeScript.of(c) == Character.UnicodeScript.HANGUL).count();
        return (double) hangul / letters;
    }

    /** null 과 문자열 "null" 을 모두 빈 값으로 본다 (모델이 필드를 빠뜨린 경우). */
    private static String textOrEmpty(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value).trim();
        return "null".equalsIgnoreCase(text) ? "" : text;
    }

    /**
     * 퀴즈 유형에 쓰이지 않는 필드를 버린다 (2026-09-22 실측: 단어배열에 options 가, 객관식에 빈 tiles 가 함께 왔다).
     * 앱이 두 형식을 동시에 그리려다 어긋나는 것을 막는다.
     */
    static Map<String, Object> dropFieldsForOtherTypes(Map<String, Object> quiz) {
        if (quiz == null) {
            return null;
        }
        String quizType = String.valueOf(quiz.get("quiz_type"));
        List<String> unused = switch (quizType) {
            case "word_arrange" -> List.of("options");
            case "multiple_choice" -> List.of("tiles");
            case "subjective" -> List.of("options", "tiles");
            default -> List.of();
        };
        Map<String, Object> fixed = new HashMap<>(quiz);
        for (String field : unused) {
            if (fixed.remove(field) != null) {
                log.debug("[InteractiveStory] {} 퀴즈에서 쓰지 않는 필드 제거: {}", quizType, field);
            }
        }
        // 빈 목록도 앱에서 빈 줄로 그려지므로 함께 정리한다
        for (String field : List.of("options", "tiles", "acceptable_answers")) {
            if (fixed.get(field) instanceof List<?> list && list.isEmpty()) {
                fixed.remove(field);
            }
        }
        return fixed;
    }

    /** 퀴즈 유형별 한국어 질문 템플릿 (프롬프트의 QUIZ FORMATS 와 같은 문구). */
    static String koreanQuestionFor(String quizType, String replyMeaning) {
        return "word_arrange".equals(quizType)
                ? "'" + replyMeaning + "'가 되도록 단어를 배열해 보세요."
                : "'" + replyMeaning + "'를 뜻하는 표현은?";
    }

    /**
     * 학습자에게 보이는 설명 필드를 한국어로 맞춘다 (2026-09-22 실측: 일본어 세션에서 question·hint 가 일본어로 나와
     * 학습자가 무엇을 묻는지조차 알 수 없었다). 한국어가 아닌 question 은 reply_meaning 으로 다시 쓰고,
     * 한국어가 아닌 hint·explanation 은 버린다. reply_meaning 이 없어 다시 쓸 수 없으면 그대로 두고 거부에 맡긴다.
     */
    static Map<String, Object> normalizeKoreanFields(Map<String, Object> fixed) {
        String meaning = textOrEmpty(fixed.get("reply_meaning"));
        String question = textOrEmpty(fixed.get("question"));
        if (!hasHangul(question) && !meaning.isEmpty() && hasHangul(meaning)) {
            String rewritten = koreanQuestionFor(String.valueOf(fixed.get("quiz_type")), meaning);
            log.warn("[InteractiveStory] 퀴즈 질문이 한국어가 아니어서 다시 씀: \"{}\" -> \"{}\"", question, rewritten);
            fixed.put("question", rewritten);
        }
        for (String field : List.of("hint", "explanation")) {
            Object value = fixed.get(field);
            if (value != null && !String.valueOf(value).isBlank() && !hasHangul(String.valueOf(value))) {
                log.warn("[InteractiveStory] 퀴즈 {} 가 한국어가 아니어서 제거: \"{}\"", field, value);
                fixed.remove(field);
            }
        }
        return fixed;
    }

    /**
     * 대상 언어로 써야 할 퀴즈 필드에 한글이 섞였으면 그 필드 이름을 돌려준다 (없으면 null).
     * "asked" 는 AI 가 실제로 말하는 문장이라 한국어면 대사에 한국어가 끌려 들어가고(실측 2026-09-22),
     * 정답·보기·타일은 학습자가 읽거나 조립해야 할 대상 언어 표현이라 한글이 섞이면 풀 수 없는 문제가 된다
     * (실측: 정답이 "看完中경삼림我感觉很特别").
     */
    static String wrongScriptField(String targetLanguage, Map<String, Object> quiz) {
        if (quiz == null) {
            return null;
        }
        for (String field : List.of("asked", "correct_answer")) {
            Object value = quiz.get(field);
            if (value != null && isWrongScript(targetLanguage, String.valueOf(value))) {
                return field;
            }
        }
        for (String field : List.of("options", "tiles", "acceptable_answers")) {
            for (String value : stringList(quiz.get(field))) {
                if (isWrongScript(targetLanguage, value)) {
                    return field;
                }
            }
        }
        return null;
    }

    /**
     * 대상 언어로 써야 할 값이 다른 문자 체계로 쓰였는지. 한글은 모든 언어에서 잘못된 것이고,
     * 일본어·중국어 세션에서는 로마자 위주 문장도 잘못된 것이다
     * (실측 2026-09-22: asked 가 "Is that friend from the Chiikawa series?" 로 와서 일본어 대사 끝에 영어가 붙었다).
     */
    private static boolean isWrongScript(String targetLanguage, String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        if (hasUnexpectedHangul(targetLanguage, value)) {
            return true;
        }
        if (LearningLanguage.usesSpaces(targetLanguage)) {
            return false; // 영어 등 로마자 언어는 로마자가 정상이다
        }
        long letters = value.chars().filter(Character::isLetter).count();
        if (letters < 4) {
            return false; // 외래어 표기나 고유명사 한두 글자는 정상
        }
        long latin = value.chars()
                .filter(Character::isLetter)
                .filter(c -> Character.UnicodeScript.of(c) == Character.UnicodeScript.LATIN)
                .count();
        return latin * 10 >= letters * 6;
    }

    /**
     * 학습자에게 보여 주는 한국어 안내문(question) 안에 정답이 그대로 들어 있으면 그 정답을 돌려준다 (없으면 null).
     *
     * 실측(2026-09-22): reply_meaning 이 한국어·일본어 혼용("나는駅前のカフェに行きたいよ。")으로 와서 안내문이
     * "'나는駅前のカフェに行きたいよ。'를 뜻하는 표현은?" 이 됐다. 학습자는 안내문을 그대로 베껴 쓰면 되고,
     * 정작 정답은 짧은 '駅前のカフェ' 라서 베낀 답이 미시도로 처리됐다. 앵무새 검사는 대사만 보기 때문에 못 잡는다.
     */
    static String answerEchoedInQuestion(Map<String, Object> quiz, String targetLanguage) {
        if (quiz == null) {
            return null;
        }
        String question = quiz.get("question") == null ? "" : String.valueOf(quiz.get("question"));
        if (question.isBlank()) {
            return null;
        }
        if (LearningLanguage.usesSpaces(targetLanguage)) {
            List<String> questionWords = gradingWords(question);
            for (String answer : acceptedAnswers(quiz)) {
                List<String> answerWords = gradingWords(answer);
                if (!answerWords.isEmpty() && Collections.indexOfSubList(questionWords, answerWords) >= 0) {
                    return answer;
                }
            }
            return null;
        }
        String compactQuestion = compactForGrading(question);
        for (String answer : acceptedAnswers(quiz)) {
            String compact = compactForGrading(answer);
            if (compact.length() >= 2 && compactQuestion.contains(compact)) {
                return answer;
            }
        }
        return null;
    }

    /** 배울 것이 없는 맞장구 정답 (예/아니오/응). 이런 퀴즈는 턴만 쓰고 남는 것이 없다. */
    private static final java.util.Set<String> FILLER_ANSWERS = java.util.Set.of(
            "はい", "いいえ", "うん", "ううん", "ええ", "そう", "そうです", "yes", "no", "yeah", "yep", "nope",
            "ok", "okay", "sure", "是", "不是", "对", "不对", "嗯", "好", "好的", "有", "没有");

    /** 정답이 맞장구 한 마디뿐이면 그 값을 돌려준다 (없으면 null). */
    static String fillerOnlyAnswer(Map<String, Object> quiz) {
        if (quiz == null) {
            return null;
        }
        String correct = quiz.get("correct_answer") == null ? "" : String.valueOf(quiz.get("correct_answer"));
        String compact = compactForGrading(correct);
        return FILLER_ANSWERS.contains(compact) ? correct : null;
    }

    /**
     * 두 표현이 사실상 같은 내용인지 (띄어쓰기 없는 언어용). 글자 구성의 겹침 비율로 본다.
     * "続けてベットします" 와 "ベットを続けます" 처럼 어순·어미만 바꾼 같은 표현을 잡는다 (실측 2026-09-22).
     */
    static boolean sameContentNoSpace(String a, String b) {
        String x = compactForGrading(a);
        String y = compactForGrading(b);
        if (x.isEmpty() || y.isEmpty()) {
            return false;
        }
        if (x.equals(y)) {
            return true;
        }
        if (Math.min(x.length(), y.length()) < 3) {
            return false; // 한두 글자는 우연히 겹치기 쉬워 완전 일치만 본다
        }
        List<Character> pool = new ArrayList<>();
        for (char c : y.toCharArray()) {
            pool.add(c);
        }
        int shared = 0;
        for (char c : x.toCharArray()) {
            if (pool.remove((Character) c)) {
                shared++;
            }
        }
        return shared * 10 >= Math.min(x.length(), y.length()) * 8;
    }

    /**
     * 주관식 힌트가 정답과 어긋나면 힌트를 버린다 (2026-09-22 실측: 정답이 한 글자 "夜" 인데 힌트가 "2글자로 밤을 뜻하는
     * 단어" 라, 힌트를 따른 학습자가 오답 처리됐다). 글자 수·단어 수를 말하는 힌트만 검사하고 나머지는 그대로 둔다.
     */
    static Map<String, Object> dropMisleadingHint(Map<String, Object> quiz) {
        if (quiz == null || quiz.get("hint") == null) {
            return quiz;
        }
        String hint = String.valueOf(quiz.get("hint"));
        String answer = quiz.get("correct_answer") == null ? "" : String.valueOf(quiz.get("correct_answer"));
        if (answer.isBlank()) {
            return quiz;
        }
        java.util.regex.Matcher matcher = COUNT_CLUE.matcher(hint);
        while (matcher.find()) {
            int claimed = koreanCount(matcher.group(1));
            if (claimed <= 0) {
                continue;
            }
            boolean aboutWords = matcher.group(2).contains("단어") || matcher.group(2).contains("어절");
            int actual = aboutWords
                    ? (int) java.util.Arrays.stream(answer.trim().split("\\s+")).filter(w -> !w.isEmpty()).count()
                    : compactForGrading(answer).length();
            if (claimed != actual) {
                log.warn("[InteractiveStory] 힌트가 정답과 어긋나 제거: hint=\"{}\" (정답 \"{}\" 는 {}{})",
                        hint, answer, actual, aboutWords ? "단어" : "글자");
                Map<String, Object> fixed = new HashMap<>(quiz);
                fixed.remove("hint");
                return fixed;
            }
        }
        return quiz;
    }

    /** 띄어쓰기 없는 언어에서 "같은 질문을 또 했다"로 보는 문자 2-gram 겹침 비율. */
    static final double NO_SPACE_REPEAT_RATIO = 0.7;

    /** "2글자", "두 글자", "세 단어" 처럼 길이를 말하는 힌트를 잡는다. */
    private static final java.util.regex.Pattern COUNT_CLUE = java.util.regex.Pattern.compile(
            "([0-9]+|한|두|세|네|다섯|여섯)\\s*(글자|자|단어|어절)");

    private static int koreanCount(String token) {
        switch (token) {
            case "한": return 1;
            case "두": return 2;
            case "세": return 3;
            case "네": return 4;
            case "다섯": return 5;
            case "여섯": return 6;
            default:
                try {
                    return Integer.parseInt(token);
                } catch (NumberFormatException e) {
                    return 0;
                }
        }
    }

    /** 학습자의 대답이 아니라 제3자를 서술하는 reply_meaning ("그는 화가 난다"). */
    private static final java.util.regex.Pattern THIRD_PERSON_MEANING = java.util.regex.Pattern.compile(
            "^\\s*(그|그녀|걔|쟤|그분|이분|상대|상대방|그 사람|그 애|저 사람)(는|은|가|이)");

    /** 학습자의 대답이 아니라 제3자를 서술하는 정답 (「彼は怒っている」, "他很生气"). */
    private static final java.util.regex.Pattern THIRD_PERSON_ANSWER = java.util.regex.Pattern.compile(
            "^\\s*(彼|彼女|相手|あいつ|こいつ|そいつ|他|她|它|对方|對方)");

    /**
     * 퀴즈 정답이 학습자의 대사가 아니라 제3자에 대한 서술이면 true
     * (2026-09-22 실측: "공격한 후 상대는 어떻게 될까?" -> 「彼は怒っている」).
     */
    static boolean isThirdPersonNarration(Map<String, Object> quiz) {
        if (quiz == null) {
            return false;
        }
        String meaning = quiz.get("reply_meaning") == null ? "" : String.valueOf(quiz.get("reply_meaning"));
        if (THIRD_PERSON_MEANING.matcher(meaning).find()) {
            return true;
        }
        return acceptedAnswers(quiz).stream().anyMatch(a -> THIRD_PERSON_ANSWER.matcher(a).find());
    }

    /** 가타카나 4자 이상 연속 (지어낸 고유명사 후보). 장음 ー 포함. */
    private static final java.util.regex.Pattern KATAKANA_RUN = java.util.regex.Pattern.compile("[ァ-ヶー]{4,}");

    /**
     * 일본어 주관식 정답에 대화에서 한 번도 나온 적 없는 가타카나 고유명사가 들어 있으면 그 표현을 돌려준다
     * (2026-09-22 실측: AI 가 지어낸 필살기 이름 「シャドウバースト」를 주관식으로 출제 — 학습자가 맞힐 방법이 없다).
     * 보기가 주어지는 객관식과 타일이 주어지는 단어배열은 추측이 가능하므로 대상이 아니다.
     */
    static String unknownInventedTerm(StorySession session, Map<String, Object> quiz) {
        if (session == null || quiz == null || !"subjective".equals(String.valueOf(quiz.get("quiz_type")))) {
            return null;
        }
        if (LearningLanguage.fromPromptName(session.getTargetLanguage()).orElse(null) != LearningLanguage.JA) {
            return null;
        }
        StringBuilder known = new StringBuilder();
        known.append(session.getCharacterName() == null ? "" : session.getCharacterName())
             .append(session.getSituationDescription() == null ? "" : session.getSituationDescription());
        for (Map<String, String> message : session.getChatHistory()) {
            known.append(message.get("content") == null ? "" : message.get("content"));
        }
        String seen = known.toString();
        for (String answer : acceptedAnswers(quiz)) {
            java.util.regex.Matcher matcher = KATAKANA_RUN.matcher(answer);
            while (matcher.find()) {
                if (!seen.contains(matcher.group())) {
                    return matcher.group();
                }
            }
        }
        return null;
    }

    /** 클라이언트로 내려보낼 퀴즈 사본: 모델 자기 점검용 내부 필드("asked", "reply_meaning")를 뺀다. 세션에는 원본이 남는다. */
    static Map<String, Object> clientQuizView(Map<String, Object> quiz) {
        if (quiz == null) {
            return null;
        }
        Map<String, Object> view = new HashMap<>(quiz);
        INTERNAL_QUIZ_FIELDS.forEach(view::remove);
        return view;
    }

    private static final String CORRECT_REACTION = """
               - React to WHAT THEY SAID, not to the fact that they were right. Their answer is their real
                 reply in the scene: take its MEANING, accept it as settled, and move the story forward with it.
                 A light confirmation woven into the sentence is fine. DO NOT open with a bare verdict like
                 "정답이야!" / "Correct!", DO NOT echo their answer back as praise, and DO NOT ask again
                 the question they have just answered.
               - The answer is ALREADY judged correct. Do NOT re-check their spelling, spacing or word order,
                 do NOT point out anything "off" about how they wrote it, and NEVER ask them to say it again.
               """;

    private static final String INCORRECT_REACTION = """
               - Stay in character and let the mistake surface naturally inside the scene:
                   1. If their answer is a real expression with a DIFFERENT meaning, react briefly to that
                      meaning so the mismatch becomes obvious by itself, inside the scene.
                   2. If it is a word-order or missing-word mistake (typical for word_arrange), say only that it does
                      not sound right yet. Do NOT use the "different meaning" pattern for these.
                   3. If their answer is not a usable expression here, say so plainly but kindly.
                 DO NOT REVEAL THE CORRECT ANSWER YET - they still have tries left, and revealing it turns the retry
                 into copying. Give exactly ONE targeted hint instead:
                   * word_arrange: NEVER explain the word order and NEVER say which word should go where -
                     your word-order explanations come out wrong and contradict each other, and the learner who
                     follows them gets marked wrong again. If a tile is MISSING, name the MEANING that is missing.
                     Otherwise just say the order is not natural yet and let them try another arrangement.
                   * multiple_choice: say what their choice actually means and what meaning you are looking for,
                     never name the right option.
                   * subjective: give the first letter, the number of letters, or a meaning clue - never the word itself.
                 Then invite them to try once more. The app shows the same quiz again by itself, so do NOT ask a
                 different question and do NOT move the scene forward yet.
                 Stay in the scene: never say "the quiz", "the app", "the exercise" - you are a person talking, not a tutor.
                 NEVER pretend they said the correct expression, NEVER quietly skip past the mistake,
                 NEVER praise a wrong answer, and NEVER call it correct.
               """;

    private static final String LAST_INCORRECT_REACTION = """
               - This was their LAST allowed attempt. Stay in character and REVEAL the correct expression in this very
                 message: your ai_message MUST contain the "Correct answer" above EXACTLY, character for character -
                 the natural way is to put it in their mouth ("... is what you meant, right?"). Then treat it as their
                 reply and continue the scene naturally from it. Do NOT invite another try, do NOT ask the same question
                 again, do NOT repeat an earlier line of yours, and do NOT present a new quiz this turn.
               """;

    private static final String NOT_ATTEMPT_REACTION = """
               - Respond to what they actually said in ONE short sentence, in character (answer their
                 question, react to their remark). Then steer them back to your pending in-story question so
                 they can answer it - remind them briefly what you asked. The app shows the pending quiz again
                 by itself. Do NOT move the scene forward, do NOT open a new topic, do NOT grade anything.
               """;

    /**
     * 서버 측 답안 분류. 결정론적으로 판정할 수 있는 만큼만 판정한다.
     *   "correct"     - 정답/허용 답안과 일치
     *   "incorrect"   - 실제 시도인데 틀림 (객관식: 다른 보기를 고름, 단어배열: 타일로 만든 다른 문장)
     *   NOT_ATTEMPT   - 시도가 아님 (객관식: 보기에 없는 자유 입력, 단어배열: 타일 밖 단어가 섞인 입력)
     *   null          - 서버가 확정 불가 → 모델에 위임 (주관식 허용 목록 밖 입력)
     * 대소문자, 앞뒤 공백, 끝 문장부호, 연속 공백 차이는 무시한다.
     */
    static String classifyAnswer(Map<String, Object> quiz, String userMessage) {
        return classifyAnswer(quiz, userMessage, LearningLanguage.DEFAULT.getPromptName());
    }

    /**
     * 이 세션의 대기 퀴즈에 대한 서버 판정. 채점 지시문과 서비스가 모두 이 메서드만 쓴다 —
     * 두 곳에서 따로 계산하면 언어 규칙이 엇갈려 "정답인데 다시 풀라"는 턴이 나온다 (2026-09-22 실측).
     */
    static String serverVerdict(StorySession session, String userMessage) {
        return classifyAnswer(session.getPendingQuiz(), userMessage, session.getTargetLanguage());
    }

    /**
     * 띄어쓰기 없는 언어에서 앱이 타일을 공백으로 이어 보낸 경우, 프롬프트·히스토리에 넣을 사본에서 공백을 뺀다.
     * 타임라인에는 사용자가 실제로 보낸 원문이 남는다.
     */
    static String normalizeSubmissionForPrompt(StorySession session, String userMessage) {
        Map<String, Object> pending = session == null ? null : session.getPendingQuiz();
        if (pending == null || userMessage == null || LearningLanguage.usesSpaces(session.getTargetLanguage())) {
            return userMessage;
        }
        String stripped = userMessage.replaceAll("[\\s\\u3000]+", "");
        if (stripped.isEmpty() || stripped.equals(userMessage.trim())) {
            return userMessage;
        }
        String compact = compactForGrading(userMessage);
        boolean matchesAnswer = acceptedAnswers(pending).stream()
                .anyMatch(a -> compactForGrading(a).equals(compact));
        boolean builtFromTiles = "word_arrange".equals(String.valueOf(pending.get("quiz_type")))
                && composedOfTiles(compact, stringList(pending.get("tiles")));
        return matchesAnswer || builtFromTiles ? stripped : userMessage;
    }

    static String classifyAnswer(Map<String, Object> quiz, String userMessage, String targetLanguage) {
        if (quiz == null) {
            return null;
        }
        List<String> accepted = acceptedAnswers(quiz);
        if (accepted.isEmpty()) {
            return null; // 채점 기준이 없으면 모델에 위임
        }
        String verdict = LearningLanguage.usesSpaces(targetLanguage)
                ? classifySpacedAnswer(quiz, accepted, userMessage)
                : classifyNoSpaceAnswer(quiz, accepted, userMessage);
        // 대상 언어로 답해야 하는 자리에 한국어로 답하면 오답이다 (팀 결정 2026-09-22).
        // 모델에 맡기면 "시도 아님"과 오답 사이에서 흔들려 기회 계산이 어긋났다.
        if (!"correct".equals(verdict) && isKoreanSubmission(targetLanguage, userMessage)) {
            return "incorrect";
        }
        return verdict;
    }

    /** 대상 언어가 한국어가 아닌데 학습자가 한국어로 답했는지 (글자의 절반 이상이 한글). */
    static boolean isKoreanSubmission(String targetLanguage, String userMessage) {
        return hasUnexpectedHangul(targetLanguage, userMessage) && hangulRatio(userMessage) >= 0.5;
    }

    /** 띄어쓰기를 쓰는 언어(영어 등)의 답안 분류. */
    private static String classifySpacedAnswer(Map<String, Object> quiz, List<String> accepted, String userMessage) {
        String user = normalizeForGrading(userMessage);
        if (user.isEmpty()) {
            return NOT_ATTEMPT;
        }
        if (accepted.stream().anyMatch(a -> normalizeForGrading(a).equals(user))) {
            return "correct";
        }

        String quizType = String.valueOf(quiz.get("quiz_type"));
        if ("multiple_choice".equals(quizType)) {
            List<String> options = stringList(quiz.get("options"));
            if (options.isEmpty()) {
                return "incorrect";
            }
            boolean pickedAnOption = options.stream().anyMatch(o -> normalizeForGrading(o).equals(user));
            return pickedAnOption ? "incorrect" : NOT_ATTEMPT;
        }
        if ("word_arrange".equals(quizType)) {
            List<String> tiles = stringList(quiz.get("tiles"));
            if (tiles.isEmpty()) {
                return "incorrect";
            }
            List<String> tileWords = new ArrayList<>(tiles.stream().map(OpenAiStoryService::normalizeForGrading).toList());
            for (String word : user.split(" ")) {
                if (!tileWords.remove(word)) {
                    return NOT_ATTEMPT; // 타일에 없는 단어를 썼다 → 타일 배열 시도가 아니다
                }
            }
            // 타일을 하나도 남김없이 썼는데 순서만 다르다 → 자연스러운 어순일 수 있으니 모델이 판정한다 (2026-09-22)
            return tileWords.isEmpty() ? null : "incorrect";
        }
        // 주관식은 철자 실수 같은 유연한 판정(및 "시도 아님" 판별)의 여지를 모델에 남긴다
        return "subjective".equals(quizType) ? null : "incorrect";
    }

    /**
     * 띄어쓰기 없는 언어의 답안 분류. 공백을 모두 지운 문자열로 비교하므로 앱이 타일을 공백으로 잇든 붙여 보내든 같다.
     * 단어배열은 제출 문자열을 타일로 정확히 조립할 수 있으면 "시도(오답)", 아니면 "시도 아님"이다.
     */
    private static String classifyNoSpaceAnswer(Map<String, Object> quiz, List<String> accepted, String userMessage) {
        String user = compactForGrading(userMessage);
        if (user.isEmpty()) {
            return NOT_ATTEMPT;
        }
        if (accepted.stream().anyMatch(a -> compactForGrading(a).equals(user))) {
            return "correct";
        }
        String quizType = String.valueOf(quiz.get("quiz_type"));
        if ("multiple_choice".equals(quizType)) {
            List<String> options = stringList(quiz.get("options"));
            if (options.isEmpty()) {
                return "incorrect";
            }
            return options.stream().anyMatch(o -> compactForGrading(o).equals(user)) ? "incorrect" : NOT_ATTEMPT;
        }
        if ("word_arrange".equals(quizType)) {
            List<String> tiles = stringList(quiz.get("tiles"));
            if (tiles.isEmpty()) {
                return "incorrect";
            }
            // composedOfTiles 는 타일을 빠짐없이 한 번씩 쓴 경우만 참이다 → 순서만 다른 배열은 모델이 판정한다
            return composedOfTiles(user, tiles) ? null : NOT_ATTEMPT;
        }
        return "subjective".equals(quizType) ? null : "incorrect";
    }

    /**
     * 서버 측 결정론적 채점 (하위 호환용). classifyAnswer 의 결과 중 correct/incorrect 만 돌려주고,
     * 시도 아님과 모델 위임은 null 로 합친다.
     */
    static String gradeAnswer(Map<String, Object> quiz, String userMessage) {
        String verdict = classifyAnswer(quiz, userMessage);
        return "correct".equals(verdict) || "incorrect".equals(verdict) ? verdict : null;
    }

    private static List<String> acceptedAnswers(Map<String, Object> quiz) {
        List<String> accepted = new ArrayList<>();
        Object correct = quiz.get("correct_answer");
        if (correct != null && !String.valueOf(correct).isBlank()) {
            accepted.add(String.valueOf(correct));
        }
        accepted.addAll(stringList(quiz.get("acceptable_answers")));
        return accepted;
    }

    private static List<String> stringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (Object o : list) {
            if (o != null && !String.valueOf(o).isBlank()) {
                out.add(String.valueOf(o));
            }
        }
        return out;
    }

    /**
     * 모델이 만든 퀴즈 객체를 서버가 쓰기 전에 정리한다 (가변 복사본을 돌려준다).
     *   - word_arrange: 타일이 정답 문장의 단어와 정확히 같은 집합이 아니면(단어 누락/추가 실측),
     *     정답에서 타일을 다시 만들어 섞는다. 정답이 없고 타일만 있으면 타일을 이어 붙여 정답으로 삼는다.
     *   - multiple_choice: 정답이 보기에 없으면 보기에 정답을 넣는다 (보기가 4개를 넘으면 마지막 것을 뺀다).
     *   - subjective: correct_answer 가 없으면 acceptable_answers 의 첫 값을 대표 정답으로 채운다.
     */
    static Map<String, Object> sanitizeQuiz(Map<String, Object> quiz) {
        return sanitizeQuiz(quiz, LearningLanguage.DEFAULT.getPromptName());
    }

    static Map<String, Object> sanitizeQuiz(Map<String, Object> quiz, String targetLanguage) {
        if (quiz == null) {
            return null;
        }
        if (!LearningLanguage.usesSpaces(targetLanguage)) {
            return dropFieldsForOtherTypes(dropWordArrangeAlternatives(sanitizeNoSpaceQuiz(quiz)));
        }
        Map<String, Object> fixed = new HashMap<>(quiz);
        String quizType = String.valueOf(fixed.get("quiz_type"));
        String correct = fixed.get("correct_answer") != null ? String.valueOf(fixed.get("correct_answer")).trim() : "";

        if ("word_arrange".equals(quizType)) {
            List<String> tiles = stringList(fixed.get("tiles"));
            if (correct.isEmpty() && !tiles.isEmpty()) {
                correct = String.join(" ", tiles);
                fixed.put("correct_answer", correct);
            }
            if (!correct.isEmpty()) {
                // 정답 문장의 끝 문장부호는 타일에 들어가면 안 된다 ("sweet?" 타일 실측)
                correct = correct.replaceAll("[.,!?]+$", "").trim();
                fixed.put("correct_answer", correct);
                List<String> words = java.util.Arrays.stream(correct.split("\\s+")).filter(w -> !w.isEmpty()).toList();
                List<String> tileWords = tiles.stream().map(t -> t.trim().replaceAll("[.,!?]+$", "")).sorted(String.CASE_INSENSITIVE_ORDER).toList();
                List<String> answerWords = words.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
                if (!tileWords.equals(answerWords)) {
                    List<String> shuffled = new ArrayList<>(words);
                    Collections.shuffle(shuffled);
                    if (shuffled.equals(words) && shuffled.size() > 1) {
                        Collections.reverse(shuffled);
                    }
                    log.warn("[InteractiveStory] word_arrange 타일이 정답과 불일치하여 재생성: tiles={} -> answer=\"{}\"", tiles, correct);
                    fixed.put("tiles", shuffled);
                }
            }
        } else if ("multiple_choice".equals(quizType)) {
            List<String> options = new ArrayList<>(stringList(fixed.get("options")));
            if (!correct.isEmpty() && !options.isEmpty()) {
                String normalizedCorrect = normalizeForGrading(correct);
                boolean present = options.stream().anyMatch(o -> normalizeForGrading(o).equals(normalizedCorrect));
                if (!present) {
                    log.warn("[InteractiveStory] multiple_choice 정답이 보기에 없어 보기에 추가: answer=\"{}\", options={}", correct, options);
                    if (options.size() >= 4) {
                        options.remove(options.size() - 1);
                    }
                    options.add(Math.min(1, options.size()), correct);
                    fixed.put("options", options);
                }
            }
        } else if ("subjective".equals(quizType)) {
            List<String> acceptable = stringList(fixed.get("acceptable_answers"));
            if (correct.isEmpty() && !acceptable.isEmpty()) {
                correct = acceptable.get(0);
                fixed.put("correct_answer", correct);
            }
            // 단답 규칙을 어긴 문장형 주관식(실측: "I usually eat out")은 타이핑 대신 타일 배열로 바꾼다.
            List<String> words = java.util.Arrays.stream(correct.split("\\s+")).filter(w -> !w.isEmpty()).toList();
            if (words.size() > MAX_SUBJECTIVE_WORDS) {
                log.warn("[InteractiveStory] 문장형 주관식({}단어)을 단어배열로 변환: \"{}\"", words.size(), correct);
                List<String> shuffled = new ArrayList<>(words);
                Collections.shuffle(shuffled);
                if (shuffled.equals(words) && shuffled.size() > 1) {
                    Collections.reverse(shuffled);
                }
                fixed.put("quiz_type", "word_arrange");
                fixed.put("tiles", shuffled);
                fixed.remove("acceptable_answers");
                fixed.remove("hint");
                Object replyMeaning = fixed.get("reply_meaning");
                if (replyMeaning != null && !String.valueOf(replyMeaning).isBlank()) {
                    fixed.put("question", "'" + String.valueOf(replyMeaning).trim() + "'가 되도록 단어를 배열해 보세요.");
                }
            }
        }
        warnIfStockExpression(fixed, correct);
        return dropFieldsForOtherTypes(dropWordArrangeAlternatives(dropMisleadingHint(normalizeKoreanFields(fixed))));
    }

    /**
     * 단어배열의 acceptable_answers 는 "같은 타일을 다르게 배열한 문장"만 남긴다 (2026-09-22).
     * 어순이 여러 개인 문장에서 정답을 하나만 인정하던 문제의 대책인데, 모델이 타일에 없는 낱말을 넣은 답을 끼워 넣으면
     * 학습자가 만들 수 없는 정답이 생기므로 여기서 걸러낸다.
     */
    private static Map<String, Object> dropWordArrangeAlternatives(Map<String, Object> quiz) {
        if (quiz == null || !"word_arrange".equals(String.valueOf(quiz.get("quiz_type")))) {
            return quiz;
        }
        List<String> alternatives = stringList(quiz.get("acceptable_answers"));
        if (alternatives.isEmpty()) {
            return quiz;
        }
        // 모델이 "다른 자연스러운 어순"을 적으라고 하면 기계적으로 순열을 나열해, 일본어로 성립하지 않는 배열까지
        // 정답 목록에 넣었다 (실측 2026-09-22: "続けますベットを"). 그런 목록을 믿으면 틀린 배열이 정답이 된다.
        // 그래서 목록은 버리고, 타일을 모두 쓴 다른 어순은 그 턴에 모델이 판정하게 한다 (classifyAnswer).
        log.debug("[InteractiveStory] 단어배열 허용 어순 목록은 쓰지 않는다 (모델 판정으로 대체): {}", alternatives);
        Map<String, Object> fixed = new HashMap<>(quiz);
        fixed.remove("acceptable_answers");
        return fixed;
    }

    /**
     * 프롬프트에서 장면 예시를 모두 걷어낸 뒤에도(2026-09-22) 모델이 학습된 상투 장면을 끌어오는지 관찰하는 로그.
     * 카페·도예 교실 같은 장면은 실제로 그 상황일 수 있어 거부하지 않고 경고만 남긴다.
     */
    private static void warnIfStockExpression(Map<String, Object> quiz, String correct) {
        if (correct != null && !correct.isEmpty()
                && PROMPT_EXAMPLE_ANSWERS.contains(normalizeForGrading(correct))) {
            log.warn("[InteractiveStory] 상투 표현이 퀴즈 정답으로 나옴 (예시 베끼기 의심): \"{}\"", correct);
        }
        Object meaning = quiz.get("reply_meaning");
        if (meaning != null && STOCK_KOREAN_MEANINGS.stream()
                .anyMatch(stock -> String.valueOf(meaning).contains(stock))) {
            log.warn("[InteractiveStory] 상투 장면의 한국어 대사가 reply_meaning 으로 나옴: \"{}\"", meaning);
        }
    }

    /**
     * 띄어쓰기 없는 언어(일본어·중국어)의 퀴즈 정리 (2026-09-22). 형태소 분석이 없어 서버가 타일을 다시 만들 수 없으므로
     * 모델의 타일을 믿고, 정답이 비면 타일을 그대로 이어 붙여 만든다. 타일이 정답을 이루지 못하거나 주관식이 너무 길면
     * 여기서 고치지 않고 structuralProblem 이 거부해 재생성한다.
     */
    private static Map<String, Object> sanitizeNoSpaceQuiz(Map<String, Object> quiz) {
        Map<String, Object> fixed = new HashMap<>(quiz);
        String quizType = String.valueOf(fixed.get("quiz_type"));
        String correct = fixed.get("correct_answer") != null ? String.valueOf(fixed.get("correct_answer")).trim() : "";

        if ("word_arrange".equals(quizType)) {
            List<String> tiles = stringList(fixed.get("tiles")).stream()
                    .map(t -> t.trim().replaceAll("[.,!?。！？]+$", ""))
                    .filter(t -> !t.isEmpty())
                    .toList();
            fixed.put("tiles", tiles);
            if (correct.isEmpty() && !tiles.isEmpty()) {
                correct = String.join("", tiles);
            }
            // 정답은 공백 없이, 끝 문장부호 없이 둔다 (앱이 타일을 어떻게 잇든 채점은 compactForGrading 으로 맞춘다)
            correct = correct.replaceAll("[\\s\\u3000]+", "").replaceAll("[.,!?。！？]+$", "");
            fixed.put("correct_answer", correct);
        } else if ("multiple_choice".equals(quizType)) {
            List<String> options = new ArrayList<>(stringList(fixed.get("options")));
            if (!correct.isEmpty() && !options.isEmpty()) {
                String normalizedCorrect = compactForGrading(correct);
                boolean present = options.stream().anyMatch(o -> compactForGrading(o).equals(normalizedCorrect));
                if (!present) {
                    log.warn("[InteractiveStory] multiple_choice 정답이 보기에 없어 보기에 추가: answer=\"{}\", options={}", correct, options);
                    if (options.size() >= 4) {
                        options.remove(options.size() - 1);
                    }
                    options.add(Math.min(1, options.size()), correct);
                    fixed.put("options", options);
                }
            }
        } else if ("subjective".equals(quizType)) {
            List<String> acceptable = stringList(fixed.get("acceptable_answers"));
            if (correct.isEmpty() && !acceptable.isEmpty()) {
                fixed.put("correct_answer", acceptable.get(0));
            }
        }
        warnIfStockExpression(fixed, correct);
        return dropFieldsForOtherTypes(dropMisleadingHint(normalizeKoreanFields(fixed)));
    }

    /**
     * 띄어쓰기 없는 언어에서 서버가 고칠 수 없는 구조 문제 (2026-09-22). 거부 사유(모델에게 보낼 영어 문장)를 돌려주고
     * 문제가 없으면 null. 띄어쓰기를 쓰는 언어는 sanitizeQuiz 가 이미 고쳤으므로 항상 null.
     *   - word_arrange: 타일이 없거나, 이어 붙여도 정답이 안 되거나, 타일이 하나뿐(정답 노출)
     *   - subjective: 정답이 MAX_SUBJECTIVE_CHARS 자를 넘음 (서버가 타일로 바꿔 줄 수 없다)
     */
    static String structuralProblem(Map<String, Object> quiz, String targetLanguage) {
        if (quiz == null || LearningLanguage.usesSpaces(targetLanguage)) {
            return null;
        }
        String quizType = String.valueOf(quiz.get("quiz_type"));
        String rawCorrect = quiz.get("correct_answer") == null ? "" : String.valueOf(quiz.get("correct_answer"));
        String correct = compactForGrading(rawCorrect);
        if ("word_arrange".equals(quizType)) {
            List<String> tiles = stringList(quiz.get("tiles"));
            if (tiles.isEmpty() || !composedOfTiles(correct, tiles)) {
                return "the word_arrange tiles " + tiles + " do not join up into correct_answer \"" + rawCorrect
                        + "\" - the tiles must be the exact chunks of the answer (joined with no separator), 3 to 6 of them";
            }
            if (tiles.size() < 2) {
                return "a word_arrange quiz with a single tile gives the answer away - split the reply \"" + rawCorrect
                        + "\" into 3 to 6 natural chunks";
            }
        } else if ("subjective".equals(quizType) && correct.length() > MAX_SUBJECTIVE_CHARS) {
            return "the subjective answer \"" + rawCorrect + "\" is " + correct.length() + " characters long; in "
                    + targetLanguage + " a subjective answer is at most " + MAX_SUBJECTIVE_CHARS
                    + " characters - ask for a shorter expression, or make it a word_arrange quiz with tiles";
        }
        return null;
    }

    /** 주관식 답의 최대 단어 수. 넘으면 단어배열로 변환한다 (팀 결정: 주관식은 단답만). */
    static final int MAX_SUBJECTIVE_WORDS = 3;

    /** 띄어쓰기 없는 언어의 주관식 답 최대 글자 수 (공백·문장부호 제외). 넘으면 거부해 재생성한다 (2026-09-22). */
    static final int MAX_SUBJECTIVE_CHARS = 10;

    /** 예전 프롬프트 예시이자 모델이 잘 끌어오는 상투 표현. 정답으로 나오면 의심 로그를 남긴다 (거부는 하지 않는다). */
    private static final java.util.Set<String> PROMPT_EXAMPLE_ANSWERS = java.util.Set.of("iced", "often", "green tea");

    /** 프롬프트에서 지운 예시 장면의 한국어 대사. reply_meaning 에 나오면 상투 장면을 끌어온 것이다 (관찰용 로그). */
    private static final List<String> STOCK_KOREAN_MEANINGS =
            List.of("창가 자리", "물레", "접시로 할래", "주로 저녁에 연습해", "딸기 케이크", "감자튀김");

    /** 선택형 질문("A or B?")을 나타내는 접속사. 이런 질문은 답 단어가 질문에 나오는 것이 자연스럽다. */
    private static final List<String> ALTERNATIVE_MARKERS = List.of(" or ", "それとも", "还是", "還是", "または");

    /**
     * 정답(또는 허용 답안)이 같은 턴의 AI 대사 안에 단어 단위로 그대로 들어 있으면 true (앵무새 퀴즈).
     *   - 대사가 선택형 질문("hot or iced?", "alone or with friends?")이면 3단어 이하 답은 통과시킨다.
     *     선택지가 질문에 나오는 것은 자연스럽기 때문이다 (실측에서 과잉 차단 확인).
     *   - 그 외에는 모두 걸러낸다 ("Do you have a favorite song?" → 'favorite song' / 'favorite').
     * 단어 배열(문장 전체가 정답)은 대상에서 제외한다.
     */
    static boolean isAnswerEchoedInMessage(String aiMessage, Map<String, Object> quiz) {
        return isAnswerEchoedInMessage(aiMessage, quiz, LearningLanguage.DEFAULT.getPromptName());
    }

    static boolean isAnswerEchoedInMessage(String aiMessage, Map<String, Object> quiz, String targetLanguage) {
        if (aiMessage == null || quiz == null) {
            return false;
        }
        if (!LearningLanguage.usesSpaces(targetLanguage)) {
            return isAnswerEchoedInNoSpaceMessage(aiMessage, quiz);
        }
        List<String> messageWords = gradingWords(aiMessage);
        if (messageWords.isEmpty()) {
            return false;
        }
        boolean wordArrange = "word_arrange".equals(String.valueOf(quiz.get("quiz_type")));
        String lowered = " " + aiMessage.toLowerCase() + " ";
        boolean alternativeQuestion = ALTERNATIVE_MARKERS.stream().anyMatch(lowered::contains);
        for (String answer : acceptedAnswers(quiz)) {
            List<String> answerWords = gradingWords(answer);
            if (wordArrange) {
                // 단어배열은 문장 전체가 정답이다. 4단어 이상인데 그 단어의 80%% 이상이 대사에 있으면
                // "내 질문을 배열시키는" 퀴즈다 (실측: "What exactly did the boss tell Selena to do?" → 같은 문장 배열)
                long found = answerWords.stream().filter(messageWords::contains).count();
                if (answerWords.size() >= 4 && found * 10 >= answerWords.size() * 8) {
                    return true;
                }
                continue;
            }
            if (answerWords.isEmpty() || Collections.indexOfSubList(messageWords, answerWords) < 0) {
                continue;
            }
            if (wordArrange) {
                continue; // 단어배열은 아래에서 단어 포함률로 따로 본다
            }
            if (!alternativeQuestion || answerWords.size() > 3) {
                return true;
            }
        }
        return false;
    }

    /**
     * 띄어쓰기 없는 언어판 앵무새 검사 (2026-09-22 개정). 정답 글자의 70% 이상이 AI 대사 안에 **통째로 이어져** 있을 때만
     * 앵무새로 본다 (최장 공통 부분문자열 기준).
     *
     * 낱말 단위로 보던 이전 방식은 중국어·일본어에서 정상 퀴즈를 걸러냈다. 실측(2026-09-22, 중국어 세션):
     * "您经常听什么样的音乐？"(어떤 음악을 자주 들으세요?) 에 "我经常听嘻哈音乐"(힙합을 자주 들어요) 로 답하는 정상 퀴즈가,
     * 낱말(经常·听·音乐)이 질문과 겹친다는 이유로 세 턴 연속 거부돼 퀴즈가 한참 늦게 나왔다. 이 두 언어는 대답이 질문의
     * 낱말을 되받는 것이 자연스럽다. 반면 AI 가 자기 문장을 그대로 배열시키는 진짜 앵무새는 긴 구간이 통째로 겹치므로
     * 이 기준으로 그대로 걸린다.
     *
     * 선택형 질문(それとも/还是/または)의 짧은 답은 질문에 나오는 것이 정상이라 통과시킨다.
     */
    private static boolean isAnswerEchoedInNoSpaceMessage(String aiMessage, Map<String, Object> quiz) {
        String message = compactForGrading(aiMessage);
        if (message.isEmpty()) {
            return false;
        }
        String lowered = " " + aiMessage.toLowerCase() + " ";
        boolean alternativeQuestion = ALTERNATIVE_MARKERS.stream().anyMatch(lowered::contains);
        for (String answer : acceptedAnswers(quiz)) {
            String compact = compactForGrading(answer);
            // 짧은 낱말은 질문에 나오는 것이 자연스럽다 (팀 결정 2026-09-22).
            // 실측: AI 가 "近くのカフェに行かない？" 라고 말한 직후 'カフェ' 를 묻는 정상 퀴즈가 계속 거부돼
            // 퀴즈가 뒤로 밀렸다. 베끼기는 안내문 검사(answerEchoedInQuestion)가 막는다.
            if (compact.length() < NO_SPACE_ECHO_MIN_LENGTH) {
                continue;
            }
            double covered = (double) longestCommonSubstringLength(compact, message) / compact.length();
            if (covered < NO_SPACE_ECHO_RATIO) {
                continue;
            }
            if (alternativeQuestion && compact.length() <= MAX_SUBJECTIVE_CHARS) {
                continue;
            }
            return true;
        }
        return false;
    }

    /** 띄어쓰기 없는 언어에서 앵무새로 판정하는 겹침 비율 (정답 글자 대비 최장 공통 부분문자열 길이). */
    static final double NO_SPACE_ECHO_RATIO = 0.7;

    /** 띄어쓰기 없는 언어에서 앵무새 검사를 적용하는 최소 정답 길이. 이보다 짧은 낱말은 대사에 나와도 통과시킨다. */
    static final int NO_SPACE_ECHO_MIN_LENGTH = 5;

    /** 두 문자열의 최장 공통 부분문자열 길이 (연속 구간). 문장이 짧아 단순 DP 로 충분하다. */
    static int longestCommonSubstringLength(String a, String b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
            return 0;
        }
        int[] previous = new int[b.length() + 1];
        int best = 0;
        for (int i = 1; i <= a.length(); i++) {
            int[] current = new int[b.length() + 1];
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    current[j] = previous[j - 1] + 1;
                    best = Math.max(best, current[j]);
                }
            }
            previous = current;
        }
        return best;
    }

    /** 퀴즈 객체에서 모델의 자기 점검용 필드("asked", "reply_meaning")를 뽑아 클라이언트로 나가지 않게 한다. */
    static final List<String> INTERNAL_QUIZ_FIELDS = List.of("asked", "reply_meaning", "learner_told_me", "next_beat");

    /**
     * 모델이 퀴즈 객체에 적은 "asked"(방금 한 질문)가 실제 AI 대사 안에 있는지 (단어 단위, 70%% 이상 겹치면 통과).
     * "asked" 필드가 없으면 검사하지 않는다 (통과).
     */
    static boolean isQuizLinkedToMessage(String aiMessage, Map<String, Object> quiz) {
        return isQuizLinkedToMessage(aiMessage, quiz, LearningLanguage.DEFAULT.getPromptName());
    }

    static boolean isQuizLinkedToMessage(String aiMessage, Map<String, Object> quiz, String targetLanguage) {
        if (quiz == null || quiz.get("asked") == null) {
            return true;
        }
        if (!LearningLanguage.usesSpaces(targetLanguage)) {
            // 단어 경계가 없으므로 문자열 포함 또는 문자 2-gram 겹침 70%% 이상이면 대사에 있는 질문으로 본다
            String asked = compactForGrading(String.valueOf(quiz.get("asked")));
            if (asked.isEmpty()) {
                return true;
            }
            String message = compactForGrading(aiMessage == null ? "" : aiMessage);
            return message.contains(asked) || bigramOverlap(asked, message) >= 0.7;
        }
        List<String> askedWords = gradingWords(String.valueOf(quiz.get("asked")));
        if (askedWords.isEmpty()) {
            return true;
        }
        List<String> messageWords = gradingWords(aiMessage == null ? "" : aiMessage);
        long found = askedWords.stream().filter(messageWords::contains).count();
        return found * 10 >= askedWords.size() * 7;
    }

    /**
     * 띄어쓰기 없는 언어의 비교용 문자열: 소문자, 모든 공백(전각 포함) 제거, 끝 문장부호(전각 포함) 제거.
     * 영어의 normalizeForGrading 에 해당한다.
     */
    static String compactForGrading(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase()
                .replaceAll("[\\s\\u3000]+", "")
                .replaceAll("[.,!?。！？]+$", "");
    }

    /** 대사에 정답이 들어 있는지 (3회 오답 공개 확인용). 띄어쓰기 없는 언어는 공백을 무시하고 본다. */
    static boolean messageContainsAnswer(String targetLanguage, String message, String answer) {
        if (message == null || answer == null || answer.isBlank()) {
            return false;
        }
        if (LearningLanguage.usesSpaces(targetLanguage)) {
            return message.toLowerCase().contains(answer.toLowerCase());
        }
        return compactForGrading(message).contains(compactForGrading(answer));
    }

    /**
     * 타일을 각각 정확히 한 번씩 이어 붙여 target(공백 없는 비교용 문자열)을 만들 수 있는지 (순서 무관, 백트래킹).
     * 띄어쓰기 없는 언어의 타일 검증과 단어배열 채점에 쓴다. 타일 수가 10개 안팎이라 비용은 무시할 수준이다.
     */
    static boolean composedOfTiles(String target, List<String> tiles) {
        if (target == null || target.isEmpty() || tiles == null || tiles.isEmpty()) {
            return false;
        }
        List<String> pool = new ArrayList<>(tiles.stream()
                .map(OpenAiStoryService::compactForGrading)
                .filter(t -> !t.isEmpty())
                .toList());
        int totalLength = pool.stream().mapToInt(String::length).sum();
        if (pool.isEmpty() || totalLength != target.length()) {
            return false;
        }
        return consumeTiles(target, 0, pool);
    }

    private static boolean consumeTiles(String target, int position, List<String> remaining) {
        if (position == target.length()) {
            return remaining.isEmpty();
        }
        for (int i = 0; i < remaining.size(); i++) {
            String tile = remaining.get(i);
            if (target.startsWith(tile, position)) {
                List<String> rest = new ArrayList<>(remaining);
                rest.remove(i);
                if (consumeTiles(target, position + tile.length(), rest)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * needle 의 문자 2-gram 중 haystack 에 있는 비율 (0~1). 띄어쓰기 없는 언어에서 단어 겹침 대신 쓴다.
     * needle 이 1자면 포함 여부, 둘 중 하나가 비면 0.
     */
    static double bigramOverlap(String needle, String haystack) {
        if (needle == null || haystack == null || needle.isEmpty() || haystack.isEmpty()) {
            return 0;
        }
        if (needle.length() < 2) {
            return haystack.contains(needle) ? 1 : 0;
        }
        int total = needle.length() - 1;
        int found = 0;
        for (int i = 0; i < total; i++) {
            if (haystack.contains(needle.substring(i, i + 2))) {
                found++;
            }
        }
        return (double) found / total;
    }

    private static List<String> gradingWords(String value) {
        String cleaned = value.toLowerCase().replaceAll("[^\\p{L}\\p{N}\\s']", " ").trim();
        if (cleaned.isEmpty()) {
            return List.of();
        }
        return java.util.Arrays.stream(cleaned.split("\\s+")).filter(w -> !w.isEmpty()).toList();
    }

    /**
     * 한국어 번역문의 말투를 판별한다. 문장 끝 어미로 존댓말(요/니다/세요/죠/까요)과 반말을 세어 다수결.
     * 문장이 없거나 판별 불가면 null.
     */
    static String detectSpeechLevel(String koreanText) {
        if (koreanText == null || koreanText.isBlank()) {
            return null;
        }
        int polite = 0;
        int casual = 0;
        for (String sentence : koreanText.split("[.!?~…]+")) {
            String s = sentence.trim().replaceAll("[\\s\"'()\\[\\]]+$", "");
            if (s.isEmpty() || !s.chars().anyMatch(c -> Character.UnicodeScript.of(c) == Character.UnicodeScript.HANGUL)) {
                continue;
            }
            if (s.matches(".*(요|니다|세요|죠|까요|습니까|셨어요|세여)$")) {
                polite++;
            } else {
                casual++;
            }
        }
        if (polite == 0 && casual == 0) {
            return null;
        }
        return polite > casual ? "존댓말" : "반말";
    }

    /**
     * 대상 언어가 한국어가 아닌데 AI 대사에 한글 문장이 섞이면 그 문장을 뗀다 (실측: 오답 반응 끝에 퀴즈 질문을 한국어로 덧붙임).
     * 글자의 30%% 이상이 한글인 문장만 지운다. 전부 지워지면 원문을 그대로 돌려준다.
     */
    static String stripHangulSentences(String targetLanguage, String aiMessage) {
        if (!hasUnexpectedHangul(targetLanguage, aiMessage)) {
            return aiMessage;
        }
        String[] sentences = aiMessage.trim().split(SENTENCE_BOUNDARY);
        String joiner = LearningLanguage.usesSpaces(targetLanguage) ? " " : "";
        StringBuilder out = new StringBuilder();
        for (String sentence : sentences) {
            long letters = sentence.chars().filter(Character::isLetter).count();
            long hangul = sentence.chars().filter(c -> Character.UnicodeScript.of(c) == Character.UnicodeScript.HANGUL).count();
            if (letters > 0 && hangul * 10 >= letters * 3) {
                continue;
            }
            if (out.length() > 0) {
                out.append(joiner);
            }
            out.append(sentence.trim());
        }
        return out.length() == 0 ? aiMessage : out.toString();
    }

    /** 대상 언어가 한국어가 아닌데 AI 대사에 한글이 섞였는지 (관측용 경고 로그에 쓴다). */
    static boolean hasUnexpectedHangul(String targetLanguage, String aiMessage) {
        if (aiMessage == null || targetLanguage == null) {
            return false;
        }
        String lang = targetLanguage.toLowerCase();
        if (lang.contains("korean") || lang.contains("한국")) {
            return false;
        }
        return aiMessage.chars().anyMatch(c -> Character.UnicodeScript.of(c) == Character.UnicodeScript.HANGUL);
    }

    private static String normalizeForGrading(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase().replaceAll("[.,!?]+$", "").replaceAll("\\s+", " ").trim();
    }

    /** 아직 덜 쓰인 퀴즈 유형을 골라 유형 쏠림을 막는다 (동률이면 목록 순서 우선). */
    static String pickNextQuizType(List<String> usedTypes) {
        String best = "multiple_choice";
        int bestCount = Integer.MAX_VALUE;
        for (String type : List.of("multiple_choice", "word_arrange", "subjective")) {
            int count = Collections.frequency(usedTypes, type);
            if (count < bestCount) {
                best = type;
                bestCount = count;
            }
        }
        return best;
    }

    private static String asText(Object value) {
        return value != null ? String.valueOf(value) : "(unknown)";
    }

    private void validateApiKey() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("OpenAI API 키가 설정되어 있지 않습니다. application.yml 또는 .env 의 OPENAI_API_KEY(qring.openai.api-key)를 설정해주세요.");
        }
    }

    private Map<String, Object> callOpenAiJson(String model, List<Map<String, String>> messages) throws Exception {
        return callOpenAiJson(model, messages, true);
    }

    /**
     * response_format=json_object 를 줘도 드물게 평문이 오는 사례가 실측되어, JSON 을 못 찾으면 한 번 다시 호출한다.
     * 두 번째도 평문이면 대사로만 감싸 돌려준다.
     */
    private Map<String, Object> callOpenAiJson(String model, List<Map<String, String>> messages, boolean retryOnPlainText) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
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
                        if (responseMap.containsKey("usage")) {
                            log.info("[OpenAI Token Usage] model={} {}", model, responseMap.get("usage"));
                        }
                        Map<String, Object> parsed = parseJsonObject(contentJson);
                        if (parsed != null && parsed.get("ai_message") != null
                                && !String.valueOf(parsed.get("ai_message")).isBlank()) {
                            return parsed;
                        }
                        if (parsed != null) {
                            // JSON 은 맞는데 대사가 빠진 경우 (실측: 기본 문구 "Got it!" 이 사용자에게 노출됨)
                            if (retryOnPlainText) {
                                log.warn("[OpenAI] 응답에 ai_message 가 없어 1회 재호출: {}", contentJson);
                                return callOpenAiJson(model, messages, false);
                            }
                            log.warn("[OpenAI] 재호출도 ai_message 가 없음: {}", contentJson);
                            return parsed;
                        }
                        if (retryOnPlainText) {
                            log.warn("[OpenAI] JSON 응답이 아니어서 1회 재호출: {}", contentJson);
                            return callOpenAiJson(model, messages, false);
                        }
                        // 재호출도 평문이면: 대사로만 쓰고 번역은 비워 둔다 (영어 원문을 번역 자리에 넣지 않는다)
                        log.warn("[OpenAI] JSON 응답이 아니어서 텍스트로 감쌈 (translation 없음): {}", contentJson);
                        return Map.of(
                                "ai_message", contentJson.trim(),
                                "translation", "",
                                "is_quiz", false,
                                "answer_result", "none",
                                "is_completed", false
                        );
                    }
                }
            }
        }
        log.error("[OpenAI API 응답 미흡] responseMap: {}", responseMap);
        throw new RuntimeException("OpenAI API로부터 유효한 응답을 받지 못했습니다.");
    }

    /**
     * 응답 본문에서 JSON 객체를 뽑아 파싱한다. 앞뒤 공백·개행, 마크다운 코드 펜스(```json ... ```),
     * 객체 앞뒤에 붙은 잡문까지 허용한다. 객체를 찾지 못하거나 파싱에 실패하면 null.
     */
    Map<String, Object> parseJsonObject(String content) {
        if (content == null) {
            return null;
        }
        String text = content.trim();
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        try {
            return objectMapper.readValue(text.substring(start, end + 1),
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("[OpenAI] JSON 파싱 실패: {}", e.getMessage());
            return null;
        }
    }
}
