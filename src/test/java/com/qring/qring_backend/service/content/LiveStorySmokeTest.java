package com.qring.qring_backend.service.content;

import com.qring.qring_backend.domain.content.StorySession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * 임시 스모크 테스트: 실제 OpenAI 를 호출해 새 프롬프트의 반응을 눈으로 확인한다.
 * STORY_LIVE_TEST=1 환경변수가 있을 때만 실행되며, .env 의 OPENAI_API_KEY 를 읽는다.
 */
@EnabledIfEnvironmentVariable(named = "STORY_LIVE_TEST", matches = "1")
class LiveStorySmokeTest {

    private static final String[] SCRIPT = {
            "이번 주에 좋아하는 가수 콘서트를 가",
            "바운디라는 가순데 알아?",
            "장르가 다양하고 취향이 잘 맞는거같아",
            "게임 얘기 할래?",
            "롤 알아?",
            "응 주로 미드 해",
            "요즘은 바빠서 주말에만 해",
            "아 배고프다",
    };

    @Test
    void runScenario() throws Exception {
        OpenAiStoryService service = new OpenAiStoryService();
        Path env = Path.of(".env");
        String apiKey = Files.readAllLines(env).stream()
                .filter(l -> l.startsWith("OPENAI_API_KEY="))
                .map(l -> l.substring("OPENAI_API_KEY=".length()).trim())
                .findFirst().orElseThrow();
        String model = System.getenv().getOrDefault("STORY_LIVE_MODEL", "gpt-4.1-mini");
        set(service, "apiKey", apiKey);
        set(service, "modelName", model);
        System.out.println("=== MODEL: " + model);

        StorySession session = StorySession.builder()
                .sessionId("live").userId(1L).characterName("영")
                .situationDescription("산책하다 만난 친구와 일상 대화").tone("다정하게")
                .targetLanguage("English").levelCode(1).build();

        Map<String, Object> opening = service.generateOpening(session);
        session.addAssistantMessage(str(opening.get("ai_message")), str(opening.get("translation")));
        session.setSpeechLevel(OpenAiStoryService.detectSpeechLevel(str(opening.get("translation"))));
        System.out.println("AI> " + opening.get("ai_message") + "\n    (" + opening.get("translation") + ")  speechLevel=" + session.getSpeechLevel());

        int scriptIndex = 0;
        int attemptsOnCurrent = 0;
        for (int turn = 0; turn < 14 && !session.isCompleted(); turn++) {
            String userMessage;
            Map<String, Object> pending = session.getPendingQuiz();
            if (pending != null) {
                // 1차: 딴 얘기 → 2차: 오답 → 3차: 정답 (첫 퀴즈에서만 풀 코스, 이후는 바로 정답)
                attemptsOnCurrent++;
                if (session.getQuizCount() == 1 && attemptsOnCurrent == 1) {
                    userMessage = "그거보다 너 저녁에 뭐 해?";
                } else if (session.getQuizCount() == 1 && attemptsOnCurrent == 2) {
                    userMessage = wrongAnswer(pending);
                } else {
                    userMessage = correctAnswer(pending);
                }
            } else {
                attemptsOnCurrent = 0;
                userMessage = scriptIndex < SCRIPT.length ? SCRIPT[scriptIndex++] : "응 좋아";
            }
            System.out.println("USER> " + userMessage);

            session.addMessage("user", userMessage);
            session.incrementTurnsSinceLastQuiz();
            Map<String, Object> res = service.generateTurnResponse(session, userMessage);

            String aiMsg = str(res.get("ai_message"));
            System.out.println("AI> " + aiMsg + "\n    (" + res.get("translation") + ")  answer_result=" + res.get("answer_result")
                    + " is_quiz=" + res.get("is_quiz") + " completed=" + res.get("is_completed")
                    + (OpenAiStoryService.hasUnexpectedHangul("English", aiMsg) ? "  !!HANGUL IN AI_MESSAGE" : ""));

            // 서버 흐름을 흉내 낸다 (InteractiveStoryService 와 같은 규칙)
            if (pending != null) {
                String verdict = OpenAiStoryService.classifyAnswer(pending, userMessage);
                if (verdict == null) {
                    String m = String.valueOf(res.get("answer_result")).toLowerCase();
                    verdict = ("correct".equals(m) || "incorrect".equals(m)) ? m : OpenAiStoryService.NOT_ATTEMPT;
                }
                System.out.println("    [server verdict] " + verdict);
                if ("correct".equals(verdict)) {
                    session.clearPendingQuiz();
                } else if ("incorrect".equals(verdict)) {
                    if (session.recordWrongAttempt() >= OpenAiStoryService.MAX_WRONG_ATTEMPTS) {
                        session.clearPendingQuiz();
                    } else {
                        session.repeatPendingQuiz();
                    }
                } else {
                    session.repeatPendingQuiz();
                }
            }
            if (session.getPendingQuiz() == null && Boolean.TRUE.equals(res.get("is_quiz")) && res.get("quiz") instanceof Map<?, ?> q) {
                @SuppressWarnings("unchecked")
                Map<String, Object> quiz = OpenAiStoryService.sanitizeQuiz((Map<String, Object>) q);
                String reason = rejection(session, aiMsg, quiz);
                if (reason != null) {
                    // 서버와 같은 재생성 경로
                    System.out.println("    [QUIZ REJECTED: " + reason + "] -> regenerate");
                    String recorded = pending == null ? "none" : String.valueOf(res.get("answer_result"));
                    Map<String, Object> redo = service.regenerateTurnWithCorrection(session, userMessage, reason, recorded);
                    aiMsg = str(redo.get("ai_message"));
                    System.out.println("AI(redo)> " + aiMsg + "\n    (" + redo.get("translation") + ")  is_quiz=" + redo.get("is_quiz"));
                    @SuppressWarnings("unchecked")
                    Map<String, Object> redoQuiz = Boolean.TRUE.equals(redo.get("is_quiz")) && redo.get("quiz") instanceof Map<?, ?> q2
                            ? OpenAiStoryService.sanitizeQuiz((Map<String, Object>) q2) : null;
                    quiz = redoQuiz;
                    reason = quiz == null ? "no quiz" : rejection(session, aiMsg, quiz);
                }
                System.out.println("    [QUIZ" + (reason == null ? "" : " REJECTED AGAIN: " + reason) + "] " + quiz);
                if (reason == null) {
                    if (quiz.get("correct_answer") != null) {
                        session.addTestedQuizSubject(String.valueOf(quiz.get("correct_answer")));
                    }
                    if (quiz.get("acceptable_answers") instanceof List<?> l) {
                        l.forEach(a -> session.addTestedQuizSubject(String.valueOf(a)));
                    }
                    session.recordQuiz(quiz);
                }
            }
            session.addAssistantMessage(aiMsg, str(res.get("translation")));
        }
    }

    private static String rejection(StorySession session, String aiMsg, Map<String, Object> quiz) {
        if (InteractiveStoryService.isDuplicateQuizSubject(session.getTestedQuizSubjects(), quiz)) {
            return "duplicate";
        }
        if (OpenAiStoryService.isAnswerEchoedInMessage(aiMsg, quiz)) {
            return "echo";
        }
        if (!OpenAiStoryService.isQuizLinkedToMessage(aiMsg, quiz)) {
            return "unlinked: asked=" + quiz.get("asked");
        }
        if (OpenAiStoryService.isReaskOfRecentUserMessage(session.recentUserMessages(OpenAiStoryService.ALREADY_KNOWN_MESSAGES), quiz)) {
            return "re-ask: reply_meaning=" + quiz.get("reply_meaning");
        }
        if (OpenAiStoryService.isQuestionAlreadyAsked(session.getChatHistory(), quiz)) {
            return "already asked: " + quiz.get("asked");
        }
        return null;
    }

    private static String correctAnswer(Map<String, Object> quiz) {
        if (quiz.get("correct_answer") != null && !String.valueOf(quiz.get("correct_answer")).isBlank()) {
            return String.valueOf(quiz.get("correct_answer"));
        }
        if (quiz.get("acceptable_answers") instanceof List<?> l && !l.isEmpty()) {
            return String.valueOf(l.get(0));
        }
        return "?";
    }

    private static String wrongAnswer(Map<String, Object> quiz) {
        String type = String.valueOf(quiz.get("quiz_type"));
        if ("multiple_choice".equals(type) && quiz.get("options") instanceof List<?> options) {
            String correct = correctAnswer(quiz);
            for (Object o : options) {
                if (!String.valueOf(o).equalsIgnoreCase(correct)) {
                    return String.valueOf(o);
                }
            }
        }
        if ("word_arrange".equals(type) && quiz.get("tiles") instanceof List<?> tiles) {
            List<String> words = new java.util.ArrayList<>(tiles.stream().map(String::valueOf).toList());
            java.util.Collections.reverse(words);
            return String.join(" ", words);
        }
        return "banana";
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private static void set(Object target, String field, String value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }
}
