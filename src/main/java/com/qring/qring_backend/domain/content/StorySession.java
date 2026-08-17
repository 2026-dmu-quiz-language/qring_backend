package com.qring.qring_backend.domain.content;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class StorySession {
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

    public void addMessage(String role, String content) {
        chatHistory.add(Map.of("role", role, "content", content));
    }

    @Builder.Default
    private List<String> testedQuizSubjects = new ArrayList<>();

    public void addTestedQuizSubject(String subject) {
        if (subject != null && !subject.trim().isEmpty()) {
            testedQuizSubjects.add(subject.trim());
        }
    }

    public void incrementQuizCount() {
        this.quizCount++;
        this.turnsSinceLastQuiz = 0;
    }

    public void incrementTurnsSinceLastQuiz() {
        this.turnsSinceLastQuiz++;
    }
}
