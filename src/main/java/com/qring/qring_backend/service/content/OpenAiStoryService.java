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
              learner can actually answer (e.g. what they want to order, where they want to sit).
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
        return String.format("""
            OUTPUT LANGUAGE (ABSOLUTE):
            - "ai_message" is written 100%% in %s. Never put Korean words or sentences inside it,
              not even a short aside or a closing remark.
            - "translation" is the Korean rendering of ai_message, nothing more.
            - Korean text inside THIS prompt (rules, [meaning: ...] notes, quiz questions) explains
              MEANING only. It is never a sentence for you to copy into ai_message.
            """, targetLanguage);
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
              a later time, or a new activity the two of you would realistically do next
              (after the cafe you walk to the park, you head to the show you talked about, you go
              shopping nearby, you run into each other again the next day). Pick the transition
              from things already mentioned in this story so it feels earned.
            - Keep it to TWO spoken sentences at most: one quick beat that moves you to the next
              place/time/activity, then ONE closed question the learner can answer with a short
              reply. [meaning of a good example: "와, 공연 진짜 좋았다! 목마른데 카페 들렀다 갈까?
              따뜻한 거 마실래, 시원한 거 마실래?"]
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
     * 서버가 퀴즈를 거부했을 때의 재생성 호출. 거부 사유를 붙여 대사와 퀴즈를 함께 다시 받는다.
     * 답안 판정(answerResult)은 첫 응답 기준으로 이미 확정되었으므로 그대로 유지하라고 알린다.
     */
    public Map<String, Object> regenerateTurnWithCorrection(StorySession session, String userMessage,
                                                            String rejectionReason, String recordedAnswerResult) {
        String turnDirective = buildTurnDirective(session, userMessage)
                + buildCorrectionDirective(rejectionReason, recordedAnswerResult);
        return generateTurn(session, buildStaticSystemPrompt(session), turnDirective);
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

            return callOpenAiJson(resolveModel(session), fullMessages);
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
               - YOU MUST REMEMBER ALL DETAILS discussed (e.g. chosen drinks, food, seating preference, plans, hobbies).
               - Keep moving the conversation FORWARD to new, natural topics within the scenario.
            3. RESPOND ACCURATELY TO USER'S ACTUAL INPUT:
               - You MUST carefully read the user's latest message and respond accurately in character!
               - If the user specifies a preference, NEVER contradict or ignore their choice. Always accept and adapt to what the user said!
            4. NO ROBOTIC TRANSLATIONESE:
               - NEVER say robotic phrases like "Thanks for answering", "That's a good opinion", and NEVER repeat the user's input verbatim
                 or echo their answer back as praise ("Favorite song, huh? Solid choice!" is FORBIDDEN).
            5. AI CONVERSATION LEADERSHIP - TALK LIKE A PERSON, NOT AN INTERVIEWER:
               - Let the learner's input steer you. In non-quiz turns (`is_quiz: false`), respond the way a real partner
                 would: react to what they said, share your own opinion, feeling, or a small detail about yourself, agree
                 or disagree, or move the scene forward with an action or a suggestion. Follow their thread when they open
                 one; you do not have to bring it back to your own agenda.
               - Do NOT end every message with a question. A question is fine roughly every second or third turn, or
                 when the scene really needs a decision from the learner. Otherwise end with a statement or a suggestion
                 they can respond to [meaning: saying you'll go with the strawberry cake / pointing at a free window
                 seat and suggesting you grab it].
               - Keep it fluid and non-forced: NEVER steer or force the conversation topic unnaturally just to create a quiz.
            6. QUIZ PACING: the THIS TURN block tells you whether this turn is a quiz turn. Obey it exactly.

            THE MOST IMPORTANT QUIZ RULE - A QUIZ IS PART OF THE CONVERSATION, NOT A POP-UP TEST:
            - On a quiz turn, your ai_message ends with ONE short CLOSED in-story question, and the quiz asks for the
              exact SHORT expression the learner would use to ANSWER it. Their correct answer IS their reply.
            - NEVER RE-ASK WHAT THEY ALREADY TOLD YOU. If the learner has already answered something - even in Korean -
              it is settled: accept it, react to it, and ask about the NEXT thing. The quiz must ask for information that
              is NOT yet in the conversation. The THIS TURN block lists what is already settled.
              FORBIDDEN (real failures): they said "감자튀김 좋다" and you asked "Would you like fries or coleslaw?";
              they said "차가운거 가자" and you asked "Do you prefer your drink hot or iced?"; they said "난 기본이 좋더라"
              and you asked "Do you usually prefer the original flavor?". Each of these asks a question that was just
              answered. Instead: "Fries it is! Want ketchup or mayo with them?" -> quiz the reply to THAT.
            - HOW TO FIND THE NEXT THING (do this instead of grabbing their last sentence): pick the NEXT BEAT OF THIS
              STORY that has not happened yet, and make your question the thing your character genuinely needs to ask
              at that moment. The learner's reply (the quiz) is their in-story decision.
                * In a dramatic scene: a demand, an accusation, a plea, a bargain, a last request, a question that
                  digs into what they just revealed. Example [meaning] - the learner says they came on the boss's
                  orders to kill you -> you: "크윽... 정말 보스가 시킨 일이야?" -> the quiz is THEIR reply, e.g.
                  word_arrange for [meaning: "미안하지만 사실 내 야망이 시킨 일이야"], or multiple_choice between
                  [meaning: "그래, 보스 명령이야" / "아니, 내가 원해서 왔어" / "그건 말할 수 없어"].
                * In an everyday scene: the next choice in the activity (size, side, seat, time, route), the next step
                  (ordering -> paying -> leaving), or a related preference they have not mentioned.
              Never ask about logistics or trivia while the scene is dramatic.
            - reply_meaning is the learner's actual spoken LINE in Korean, written as speech ("그래, 보스 명령이야",
              "다른 계획이 있어", "창가 자리로 할게"), never a description of it ("무엇을 할지", "이유를 말하기",
              "보스가 시킨 이유"). The server rejects descriptions.
            - reply_meaning must be something the learner would really say NOW, consistent with everything they have
              told you. Never make them "say" the opposite of a preference they already stated (they said they eat out;
              do not quiz "I usually cook at home").
            - This is the flow you must produce (example scene: a pottery class; meanings in Korean, actual lines in %s):
                you ask     -> [meaning: "물레 돌려 본 적 있어?"]
                quiz        -> asks for the short %s expression meaning '처음이야' (with a clue such as its first letter)
                learner     -> answers with that expression, which is also their real reply
                next turn   -> you react to the MEANING of their reply [meaning: "오, 처음이구나! 그럼 천천히 해 보자."]
                               and move on. No verdict, no repeating the question.
              This prompt deliberately gives NO example answers in %s. Build every quiz from the current scene;
              never fall back to stock words from memory unless the scene truly calls for them.
            - Your question MUST therefore be a CLOSED question whose answer is predictable: yes/no, how often, this or that,
              hot or iced, what time, which of a few obvious things.
              FORBIDDEN: open questions with unknowable answers ("Which artist are you going to see?", "What's your
              favorite song?", "What do you want to talk about?") - no fixed correct answer can exist for them.
            - THE ANSWER MUST NOT APPEAR IN YOUR QUESTION. Decide correct_answer first, then write the question WITHOUT that
              word: ask [meaning: "게임 많이 하는 편이야?"] and quiz the word for '자주' - do NOT ask "Do you play often?" and
              then quiz "often"; do NOT ask "Do you have a favorite song?" and then quiz "favorite song". The learner must
              PRODUCE the expression, not copy it from you. The server rejects such quizzes.
            - Do not fall back on frequency questions ("Do you ... often?", "How often ...?") - frequency is almost never
              the next beat of a scene. Ask about what happens next, what they choose, what they feel, what they want.
            - THE QUESTION IS ABOUT THE LEARNER, NEVER ABOUT YOU. Ask what THEY will do, choose, feel, or want ("you /
              your"). FORBIDDEN: "What will I do?", "What should I do with the revolver?", "What feeling hits me?" - that
              makes the learner narrate YOUR actions and feelings in your voice. If the next beat is your own action,
              just do it in ai_message and ask them how they respond to it. The server rejects such questions.
            - NEVER ASK A META QUESTION ABOUT LANGUAGE. "What would you say if...", "How do you say...", "What should I
              ask you?", "What question should I ask?" are FORBIDDEN - your character is not a teacher and does not know
              there is a quiz. Ask a real question that a person in this scene would ask, and let the quiz (which the app
              shows separately) ask for the words. The server rejects meta questions.
            - The quizzed expression is SHORT: one word or a 2-3 word phrase (a frequency word, a drink, a place,
              a feeling). A whole sentence is allowed ONLY in word_arrange.
            - Before finalising, check both: "If they answer this correctly, have they answered my question in the scene?"
              and "Is the answer absent from my own question?" If either fails, redesign the quiz.
            - The quiz must fit the CURRENT moment of the scene. Never rewind to an earlier topic just to have something to test.
            - The quiz is about what the LEARNER says next, never about your own line. Do NOT ask them to reproduce a sentence
              you just said ("I've been practicing support" / "Shall we go eat?" are YOUR lines, not their reply).
            - The Korean illustrations in this prompt (물레, 처음이야, 주로 저녁에 연습해) are ILLUSTRATIONS ONLY. Never
              reuse them or their scenes as your quiz unless the current scene genuinely calls for that exact reply.
            - Fill the quiz object IN ORDER. The first fields are your own notes: "learner_told_me" (one Korean line
              summarising what they have told you so far), "next_beat" (the NEW thing you will ask about and why it is
              not in learner_told_me), "reply_meaning" (Korean meaning of the reply they should give). Then
              "correct_answer" in %s, and only THEN "asked" - the question sentence, written so that it does not contain
              correct_answer. Your ai_message must end with that exact "asked" sentence. The server checks it.

            MESSAGE LENGTH: ai_message is 2-3 sentences: your in-character reaction (1-2 sentences, more when the moment is
            big) and, on a quiz turn, ONE question at the end. Never stack two questions in one message, and never write
            the same question twice in different words - the question appears exactly once, as the last sentence.

            CRITICAL LANGUAGE LEARNING QUIZ RULES (Target Language: %s):
            1. STRICT TARGET LANGUAGE LOCK (%s ONLY):
               - ALL quizzes in this session MUST test ONLY "%s". NEVER mix or introduce any other foreign language.
            2. QUIZ FORMATS (every format obeys the rule above: the answer doubles as the learner's reply):
                 Format A (multiple_choice - pick your reply):
                   - You just asked [meaning: "컵 만들래, 접시 만들래?"]
                   - question: "'접시로 할래'를 뜻하는 표현은?"
                   - options: 3 short candidate replies in %s - the correct one plus two that are plausible things to
                     say in this scene but mean something different (never nonsense fillers) ; correct_answer: the reply
                 Format B (word_arrange - build your reply, `quiz_type: "word_arrange"`):
                   - You just asked a closed question; the learner's reply is a short sentence of 4-6 words
                     (e.g. asked [meaning: "언제 연습해?"] -> reply [meaning: "주로 저녁에 연습해"]).
                   - question: "'<reply meaning in Korean>'가 되도록 단어를 배열해 보세요."
                   - correct_answer: that reply sentence in the Target Language, in the LEARNER's voice ("I ...", "Let's ..."
                     only if THEY are proposing) ; tiles: EXACTLY the words of correct_answer, shuffled, no word
                     missing and no extra word. Never make the tiles from a sentence YOU said.
                 Format C (subjective - SHORT ANSWER ONLY, `quiz_type: "subjective"`):
                   - You just asked [meaning: "물레 돌려 본 적 있어?"]
                   - question: "'처음이야'를 뜻하는, f로 시작하는 두 단어 %s 표현은?"  (give a natural clue: first letter, length, or a hint)
                   - acceptable_answers: [that expression, plus natural variants if any].
                   - The answer is ONE word or a phrase of at most 3 words. NEVER ask the learner to type a full sentence -
                     typing long sentences is tiring on a phone.
                 Format D (fill in the blank as multiple_choice):
                   - question: "다음을 완성해 보세요. '<reply with one blank>' (<Korean meaning of the blank>)" ; options: 3 candidates
            3. QUIZ TYPE VARIETY:
               - Use the REQUIRED QUIZ TYPE given in the THIS TURN block. Across a session all three types should appear.
            4. ABSOLUTE QUIZ TOPIC / WORD OBSESSION PREVENTION:
               - The THIS TURN block lists expressions already tested. NEVER test or focus on any of them again.
               - Once a specific word, phrase, or concept has been tested in a previous quiz, that word or topic MUST NOT be the main focus, question subject, or correct answer in any subsequent quiz!
               - Each quiz MUST pick a fresh, completely different Target Language expression.

            QUIZ OBJECT FORMAT (ONLY included if `is_quiz` is true; keep EXACTLY this field order):
            {
              "learner_told_me": "One Korean line: what the learner has told you so far in this scene",
              "next_beat": "One Korean line: the NEW thing you will ask about now, and why it is not already known",
              "reply_meaning": "Korean meaning of the reply the learner should give (e.g. '꽤 자주 해', '차가운 걸로')",
              "quiz_type": "multiple_choice" | "word_arrange" | "subjective",
              "correct_answer": "Exact string of the correct option / the exact sentence for word_arrange",
              "acceptable_answers": ["acceptable1"], // for subjective: short answers only
              "options": ["reply 1", "reply 2", "reply 3"], // for multiple_choice, all in the Target Language
              "tiles": ["tile1", "tile2"], // for word_arrange: exactly the words of correct_answer, shuffled
              "asked": "The closed question sentence that ends your ai_message - must NOT contain correct_answer",
              "question": "Short Korean question that QUOTES the Korean meaning of correct_answer in quotes, e.g. \"'응, 받았어'를 뜻하는 표현은?\" - the learner must be able to tell which option/word is meant",
              "explanation": "One short Korean sentence clarifying the Target Language expression",
              "hint": "Short hint in Korean", // for subjective
              "quiz_number": number (1 to %d)
            }

            OUTPUT FORMAT (Strict JSON - keep EXACTLY this field order; decide the quiz BEFORE you write the line):
            {
              "answer_result": "correct" | "incorrect" | "none",
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
                targetLanguage, targetLanguage, targetLanguage,
                targetLanguage, targetLanguage,
                quizLimit);
    }

    /** 이번 턴에만 해당하는 블록: 채점, 페이싱, 이미 아는 것 목록, 기출 표현. 히스토리 뒤에 system 메시지로 붙는다. */
    String buildTurnDirective(StorySession session, String userMessage) {
        int currentQuizCount = session.getQuizCount();
        int quizLimit = session.getQuizLimit() > 0 ? session.getQuizLimit() : MAX_QUIZ_COUNT;
        int turnsSinceLastQuiz = session.getTurnsSinceLastQuiz();
        boolean quizBudgetLeft = currentQuizCount < quizLimit;
        boolean quizPending = session.getPendingQuiz() != null;
        boolean allowQuiz = turnsSinceLastQuiz >= 2 && quizBudgetLeft && !quizPending;

        // 한도의 마지막 퀴즈를 채점하는 턴인지 — 정답이면 이 턴이 스토리의 마지막 대사가 된다
        boolean gradingFinalQuiz = quizPending && currentQuizCount >= quizLimit;

        String pacingDirective;
        if (allowQuiz) {
            String requiredType = pickNextQuizType(session.getUsedQuizTypes());
            pacingDirective = String.format("PACING RULE: Sufficient dialogue turns have passed (%d turns since last quiz). You SHOULD now present a quiz by setting `is_quiz: true`. REQUIRED QUIZ TYPE FOR THIS QUIZ: \"%s\" - set `quiz_type` to exactly this value and design the quiz in that format. Your ai_message for this turn MUST end with the ONE closed in-story question that the quiz answer replies to (see THE MOST IMPORTANT QUIZ RULE). "
                    + "React to what the learner just said fully and in character, then end with a question about the NEXT BEAT of the story that is not in the ALREADY KNOWN list below. The server rejects a quiz that re-asks anything already known, and rejects meta questions about language.",
                    turnsSinceLastQuiz, requiredType);
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
            5. The learner's latest message: "%s"
               React to THIS first, in character, before anything else. If it introduces a person, an event, a threat, a
               confession or an accusation, this whole turn is about that - do not change the subject.
            """, quizContextDirective, pacingDirective, currentQuizCount, quizLimit, quizLimit,
                bulletList(session.recentUserMessages(ALREADY_KNOWN_MESSAGES)),
                bulletList(session.getAskedQuestions()),
                testedSubjectsDirective, userMessage);
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
        String serverVerdict = classifyAnswer(pendingQuiz, userMessage);
        if ("correct".equals(serverVerdict)) {
            outcomeDirective = """
               - SERVER GRADING RESULT: the server has ALREADY graded this answer as CORRECT.
                 You MUST set "answer_result": "correct". Do NOT overturn this verdict.
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
                   IN THE SAME MESSAGE - close the CURRENT SCENE the way it would really end (the
                   order arrives and you enjoy it together, you finish up and get ready to leave, the
                   activity reaches its natural end), and add one short closing sentiment
                   [meaning: "오늘 진짜 즐거웠어!" / "같이 와서 좋았다."] - said in the Target Language.
                 * Keep the ending light: "this scene is done", not a definitive final farewell.
                   The story may continue somewhere else later, so do not part ways for good.
                 * Do NOT ask any new question, do NOT open a new topic, do NOT leave the scene hanging.
                 * Set "is_completed": true.
                 Otherwise (a wrong attempt with tries left, or no attempt), do not close the story yet.
               """;

        String askedLine = pendingQuiz.get("asked") == null || String.valueOf(pendingQuiz.get("asked")).isBlank()
                ? ""
                : "    Your in-story question it answers: " + pendingQuiz.get("asked") + "\n";

        return String.format("""
               QUIZ ANSWER GRADING (a quiz IS pending):
               - The quiz presented in the immediately preceding turn was:
                   Quiz type: %s
                   Question: %s
                   Correct answer: %s
                   Accepted answers: %s
               %s
               - The user's latest input ("%s") is BOTH their answer to that quiz AND their reply in the
                 story. Treat it as both.
               %s%s- Either way your reply must read as ONE natural utterance in the scene, never as
                 "verdict first, unrelated roleplay after". Do NOT design a new quiz this turn.
               """, quizType, question, correctAnswer, acceptableAnswers, askedLine, userMessage, outcomeDirective, closingDirective);
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
        if (quiz == null || chatHistory == null || quiz.get("asked") == null) {
            return false;
        }
        List<String> askedWords = gradingWords(String.valueOf(quiz.get("asked")));
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

    /** 퀴즈의 asked 가 AI 자신에 대한 질문이면 true ("나는 뭘 할까?"). "should I go first or you?" 같은 선택 제시는 or 가 있으면 통과. */
    static boolean isQuestionAboutAiItself(Map<String, Object> quiz) {
        if (quiz == null || quiz.get("asked") == null) {
            return false;
        }
        String asked = String.valueOf(quiz.get("asked"));
        if (!SELF_QUESTION.matcher(asked).find()) {
            return false;
        }
        return !asked.toLowerCase().contains(" or ") || asked.toLowerCase().contains(" or i ");
    }

    /** 퀴즈의 asked 또는 AI 대사가 언어 메타 질문이면 true. */
    static boolean isMetaLanguageQuestion(String aiMessage, Map<String, Object> quiz) {
        String asked = quiz != null && quiz.get("asked") != null ? String.valueOf(quiz.get("asked")) : "";
        return META_QUESTION.matcher(asked).find()
                || (aiMessage != null && META_QUESTION.matcher(lastSentence(aiMessage)).find());
    }

    private static String lastSentence(String text) {
        String[] parts = text.trim().split("(?<=[.!?])\\s+");
        return parts.length == 0 ? "" : parts[parts.length - 1];
    }

    /**
     * 대사 끝에 같은 질문이 두 번 붙는 실측 사례("Cola or juice? Do you want me to bring cola or juice ...?")를 정리한다.
     * 마지막 문장과 단어가 60%% 이상 겹치는 앞 질문 문장을 지운다. 문장이 둘 이하이거나 겹치지 않으면 그대로 돌려준다.
     */
    static String removeDuplicateTrailingQuestion(String aiMessage) {
        if (aiMessage == null) {
            return null;
        }
        String[] sentences = aiMessage.trim().split("(?<=[.!?])\\s+");
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
            fixed.put("question", "'" + meaning + "'를 뜻하는 표현은?");
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
                 reply in the scene: take its MEANING and move the story forward with it
                 [meaning: they said it's their first time -> "오, 처음이구나! 그럼 천천히 해 보자." ;
                  they picked the plate -> you hand them the clay for a plate and say what you'll make].
                 A light confirmation woven into the sentence is fine. DO NOT open with a bare verdict like
                 "정답이야!" / "Correct!", DO NOT echo their answer back as praise, and DO NOT ask again
                 the question they have just answered.
               """;

    private static final String INCORRECT_REACTION = """
               - Stay in character and let the mistake surface naturally inside the scene:
                   1. If their answer is a real expression with a DIFFERENT meaning, react briefly to that
                      meaning so the mismatch becomes obvious by itself [meaning: target was 녹차 but they
                      answered "black coffee" -> "어, 블랙커피? 아까 녹차 마시고 싶다며!"].
                   2. If it is a word-order or missing-word mistake (typical for word_arrange), point at what
                      is missing or misplaced [meaning: "거의 맞았어! 'to'가 빠졌네."]. Do NOT use the
                      "different meaning" pattern for these.
                   3. If their answer is not a usable expression here, say so plainly but kindly.
                 DO NOT REVEAL THE CORRECT ANSWER YET - they still have tries left, and revealing it turns the retry
                 into copying. Give exactly ONE targeted hint instead:
                   * word_arrange: name the ONE word that is misplaced or missing and where it belongs
                     [meaning: "'usually'는 'I' 바로 뒤에 와야 해", "'at'이 빠졌어"], never the whole sentence.
                   * multiple_choice: say what their choice actually means and what meaning you are looking for
                     [meaning: "그건 삶았다는 뜻이야. 얼음 넣은 걸 뭐라고 하지?"], never name the right option.
                   * subjective: give the first letter, the number of letters, or a meaning clue - never the word itself.
                 Then invite them to try once more. The app shows the same quiz again by itself, so do NOT ask a
                 different question and do NOT move the scene forward yet.
                 Stay in the scene: never say "the quiz", "the app", "the exercise" - you are a person talking, not a tutor.
                 NEVER pretend they said the correct expression, NEVER quietly skip past the mistake,
                 NEVER praise a wrong answer, and NEVER call it correct.
               """;

    private static final String LAST_INCORRECT_REACTION = """
               - This was their LAST allowed attempt. Stay in character and REVEAL the correct expression in this very
                 message: your ai_message MUST contain the "Correct answer" above EXACTLY, character for character
                 (e.g. put it in their mouth: [meaning: "...'relieved'라고 하고 싶었던 거지?"]). Then treat it as their
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
        if (quiz == null) {
            return null;
        }
        List<String> accepted = acceptedAnswers(quiz);
        if (accepted.isEmpty()) {
            return null; // 채점 기준이 없으면 모델에 위임
        }
        String user = normalizeForGrading(userMessage);
        if (user.isEmpty()) {
            return NOT_ATTEMPT;
        }
        boolean matches = accepted.stream().anyMatch(a -> normalizeForGrading(a).equals(user));
        if (matches) {
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
            return "incorrect";
        }
        // 주관식은 철자 실수 같은 유연한 판정(및 "시도 아님" 판별)의 여지를 모델에 남긴다
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
        if (quiz == null) {
            return null;
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
        if (!correct.isEmpty() && PROMPT_EXAMPLE_ANSWERS.contains(normalizeForGrading(correct))) {
            log.warn("[InteractiveStory] 프롬프트 예시 표현이 퀴즈 정답으로 나옴 (예시 베끼기 의심): \"{}\"", correct);
        }
        return fixed;
    }

    /** 주관식 답의 최대 단어 수. 넘으면 단어배열로 변환한다 (팀 결정: 주관식은 단답만). */
    static final int MAX_SUBJECTIVE_WORDS = 3;

    /** 프롬프트 예시에 쓰인 표현. 정답으로 나오면 예시 베끼기 의심 로그를 남긴다. */
    private static final java.util.Set<String> PROMPT_EXAMPLE_ANSWERS = java.util.Set.of("iced", "often", "green tea");

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
        if (aiMessage == null || quiz == null) {
            return false;
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

    /** 퀴즈 객체에서 모델의 자기 점검용 필드("asked", "reply_meaning")를 뽑아 클라이언트로 나가지 않게 한다. */
    static final List<String> INTERNAL_QUIZ_FIELDS = List.of("asked", "reply_meaning", "learner_told_me", "next_beat");

    /**
     * 모델이 퀴즈 객체에 적은 "asked"(방금 한 질문)가 실제 AI 대사 안에 있는지 (단어 단위, 70%% 이상 겹치면 통과).
     * "asked" 필드가 없으면 검사하지 않는다 (통과).
     */
    static boolean isQuizLinkedToMessage(String aiMessage, Map<String, Object> quiz) {
        if (quiz == null || quiz.get("asked") == null) {
            return true;
        }
        List<String> askedWords = gradingWords(String.valueOf(quiz.get("asked")));
        if (askedWords.isEmpty()) {
            return true;
        }
        List<String> messageWords = gradingWords(aiMessage == null ? "" : aiMessage);
        long found = askedWords.stream().filter(messageWords::contains).count();
        return found * 10 >= askedWords.size() * 7;
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
        String[] sentences = aiMessage.trim().split("(?<=[.!?])\s+");
        StringBuilder out = new StringBuilder();
        for (String sentence : sentences) {
            long letters = sentence.chars().filter(Character::isLetter).count();
            long hangul = sentence.chars().filter(c -> Character.UnicodeScript.of(c) == Character.UnicodeScript.HANGUL).count();
            if (letters > 0 && hangul * 10 >= letters * 3) {
                continue;
            }
            if (out.length() > 0) {
                out.append(' ');
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
