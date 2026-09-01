package com.qring.qring_backend.domain.content;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class StorySession {

    /** OpenAI에 함께 보낼 최대 대화 메시지 수 (초과 시 오래된 메시지부터 제거). */
    private static final int MAX_HISTORY_MESSAGES = 40;

    private String sessionId;
    private Long userId;
    private String characterName;
    private String situationDescription;
    private String tone;
    private String targetLanguage;
    private int levelCode;

    @Builder.Default
    private int quizCount = 0;

    @Builder.Default
    private int turnsSinceLastQuiz = 0;

    @Builder.Default
    private boolean isCompleted = false;

    @Builder.Default
    private List<Map<String, String>> chatHistory = new ArrayList<>();

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private List<String> testedQuizSubjects = new ArrayList<>();

    /** 직전 AI 턴에 출제되어 아직 채점되지 않은 퀴즈. 없으면 null. */
    private Map<String, Object> pendingQuiz;

    /**
     * 대화·퀴즈·채점 결과가 실제로 일어난 순서 그대로 쌓이는 열람용 통합 타임라인.
     * chatHistory(프롬프트용, 40개 제한)와 달리 절대 잘리지 않으며, 보관 시 이대로 저장된다.
     * 이벤트 형태:
     *   {"type":"message","role":"user","content":...}
     *   {"type":"message","role":"assistant","content":...,"translation":...}
     *   {"type":"quiz","quiz":{...}}                              — 직전 assistant 메시지와 함께 출제됨
     *   {"type":"quiz_result","quiz_number":n,"user_answer":...,"result":...} — 직전 user 메시지가 답안
     */
    @Builder.Default
    private List<Map<String, Object>> timeline = new ArrayList<>();

    public void addMessage(String role, String content) {
        appendPromptHistory(role, content);

        Map<String, Object> event = new HashMap<>();
        event.put("type", "message");
        event.put("role", role);
        event.put("content", content != null ? content : "");
        timeline.add(event);
    }

    /** AI 대사는 한국어 번역까지 타임라인에 남긴다 (프롬프트 히스토리에는 원문만 들어간다). */
    public void addAssistantMessage(String content, String translation) {
        appendPromptHistory("assistant", content);

        Map<String, Object> event = new HashMap<>();
        event.put("type", "message");
        event.put("role", "assistant");
        event.put("content", content != null ? content : "");
        event.put("translation", translation != null ? translation : "");
        timeline.add(event);
    }

    private void appendPromptHistory(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content != null ? content : "");
        chatHistory.add(message);

        if (chatHistory.size() > MAX_HISTORY_MESSAGES) {
            chatHistory.subList(0, chatHistory.size() - MAX_HISTORY_MESSAGES).clear();
        }
    }

    public void addTestedQuizSubject(String subject) {
        if (subject != null && !subject.trim().isEmpty()) {
            testedQuizSubjects.add(subject.trim());
        }
    }

    /** 세션에서 이미 출제된 퀴즈 유형들 (유형 쏠림 방지용). */
    @Builder.Default
    private List<String> usedQuizTypes = new ArrayList<>();

    /** 퀴즈 출제 이벤트를 타임라인에 기록 (직전 assistant 메시지에 붙는 퀴즈). */
    public void addQuizPresented(Map<String, Object> quiz) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "quiz");
        event.put("quiz", quiz);
        timeline.add(event);
    }

    /** 채점 결과 이벤트를 타임라인에 기록 (직전 user 메시지가 제출한 답안). */
    public void addQuizResult(Map<String, Object> quiz, String userAnswer, String result) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "quiz_result");
        event.put("quiz_number", quiz != null ? quiz.get("quiz_number") : null);
        event.put("user_answer", userAnswer != null ? userAnswer : "");
        event.put("result", result);
        timeline.add(event);
    }

    /** 이번 턴에 퀴즈가 출제됨: 카운트 증가 후 다음 턴을 채점 대기 상태로 전환. */
    public void recordQuiz(Map<String, Object> quiz) {
        this.quizCount++;
        this.turnsSinceLastQuiz = 0;
        this.pendingQuiz = quiz;
        if (quiz != null && quiz.get("quiz_type") != null) {
            usedQuizTypes.add(String.valueOf(quiz.get("quiz_type")));
        }
    }

    /** 채점이 끝나 더 이상 대기 중인 퀴즈가 없음. */
    public void clearPendingQuiz() {
        this.pendingQuiz = null;
    }

    /**
     * 오답 후 같은 문제를 다시 제시한 재시도.
     * 새 퀴즈가 아니므로 quizCount 는 늘리지 않고, 대기 중인 퀴즈만 유지한다.
     */
    public void repeatPendingQuiz() {
        this.turnsSinceLastQuiz = 0;
    }

    public void incrementTurnsSinceLastQuiz() {
        this.turnsSinceLastQuiz++;
    }

    /** OpenAI 호출 실패 시 이번 턴에 반영한 사용자 입력/카운터를 되돌린다. */
    public void rollbackUserTurn() {
        if (!chatHistory.isEmpty()) {
            int lastIndex = chatHistory.size() - 1;
            if ("user".equals(chatHistory.get(lastIndex).get("role"))) {
                chatHistory.remove(lastIndex);
            }
        }
        if (!timeline.isEmpty()) {
            int lastIndex = timeline.size() - 1;
            Map<String, Object> last = timeline.get(lastIndex);
            if ("message".equals(last.get("type")) && "user".equals(last.get("role"))) {
                timeline.remove(lastIndex);
            }
        }
        if (turnsSinceLastQuiz > 0) {
            turnsSinceLastQuiz--;
        }
    }
}
