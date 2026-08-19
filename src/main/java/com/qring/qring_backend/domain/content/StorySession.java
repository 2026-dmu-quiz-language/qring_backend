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

    public void addMessage(String role, String content) {
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

    /** 이번 턴에 퀴즈가 출제됨: 카운트 증가 후 다음 턴을 채점 대기 상태로 전환. */
    public void recordQuiz(Map<String, Object> quiz) {
        this.quizCount++;
        this.turnsSinceLastQuiz = 0;
        this.pendingQuiz = quiz;
    }

    /** 채점이 끝나 더 이상 대기 중인 퀴즈가 없음. */
    public void clearPendingQuiz() {
        this.pendingQuiz = null;
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
        if (turnsSinceLastQuiz > 0) {
            turnsSinceLastQuiz--;
        }
    }
}
