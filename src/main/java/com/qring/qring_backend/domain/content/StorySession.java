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

    /**
     * OpenAI에 함께 보낼 최대 대화 메시지 수 (초과 시 오래된 메시지부터 제거).
     * 2026-09-09 팀 결정으로 40 → 80 확장 (이어하기로 길어진 세션의 맥락 유지).
     * 실측 기준 턴당 입력 토큰 상한이 약 3.3k → 5.3k 로 늘지만 비용 영향은 턴당 1원 미만.
     */
    private static final int MAX_HISTORY_MESSAGES = 80;

    private String sessionId;
    private Long userId;
    private String characterName;
    private String situationDescription;
    private String tone;
    private String targetLanguage;
    private int levelCode;

    /** 기본 퀴즈 한도 (이어하기 1회당 이만큼씩 늘어난다). */
    public static final int DEFAULT_QUIZ_LIMIT = 5;

    @Builder.Default
    private int quizCount = 0;

    /** 이 세션의 퀴즈 한도. 이어하기(연장)할 때마다 늘어난다. */
    @Builder.Default
    private int quizLimit = DEFAULT_QUIZ_LIMIT;

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

    /** 대기 중인 퀴즈에 대한 누적 오답 횟수. 새 퀴즈가 출제되면 0으로 돌아간다. */
    @Builder.Default
    private int wrongAttempts = 0;

    /**
     * 이 세션의 한국어 말투 ("반말" / "존댓말"). 오프닝 번역에서 서버가 판별해 고정한다.
     * 채점·설명 턴에서 모델이 선생님 말투(존댓말)로 흔들리는 사례가 실측되어 프롬프트에 고정값으로 넣는다. 미판별이면 null.
     */
    private String speechLevel;

    /** 모델 티어 ("standard" / "premium"). 시작 때 정해지고 이어하기에서도 바뀌지 않는다. */
    @Builder.Default
    private String modelTier = "standard";

    /** 퀴즈 턴인데 모델의 퀴즈가 (재생성까지) 거부된 횟수. 연속으로 쌓이면 서버가 부드러운 검사를 풀어 준다. 퀴즈가 수락되면 0. */
    @Builder.Default
    private int failedQuizTurns = 0;

    /** 모델이 매 턴 갱신하는 "지금까지의 이야기" 메모 (한국어 한두 줄). 다음 턴 프롬프트에 넣어 합의된 사실을 기억하게 한다. */
    private String storySoFar;

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
        addMessage(role, content, content);
    }

    /**
     * 타임라인에는 사용자가 실제로 보낸 원문을, 프롬프트 히스토리에는 모델에게 보여 줄 사본을 남긴다.
     * 띄어쓰기 없는 언어에서 타일을 공백으로 이어 보낸 답안의 공백을 떼는 데 쓴다 (2026-09-22).
     */
    public void addMessage(String role, String content, String promptContent) {
        appendPromptHistory(role, promptContent != null ? promptContent : content);

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

    /** 퀴즈로 물었던 극중 질문들(quiz.asked). 프롬프트의 "이미 물은 것" 목록과 같은 질문 반복 검사에 쓴다. */
    @Builder.Default
    private List<String> askedQuestions = new ArrayList<>();

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
        this.wrongAttempts = 0;
        this.failedQuizTurns = 0;
        if (quiz != null && quiz.get("quiz_type") != null) {
            usedQuizTypes.add(String.valueOf(quiz.get("quiz_type")));
        }
        if (quiz != null && quiz.get("asked") != null && !String.valueOf(quiz.get("asked")).isBlank()) {
            askedQuestions.add(String.valueOf(quiz.get("asked")).trim());
        }
    }

    /** 프롬프트 히스토리에서 최근 사용자 발화 n개 (오래된 것부터). */
    public List<String> recentUserMessages(int count) {
        List<String> out = new ArrayList<>();
        for (int i = chatHistory.size() - 1; i >= 0 && out.size() < count; i--) {
            if ("user".equals(chatHistory.get(i).get("role"))) {
                out.add(0, chatHistory.get(i).get("content"));
            }
        }
        return out;
    }

    /** 채점이 끝나 더 이상 대기 중인 퀴즈가 없음. */
    public void clearPendingQuiz() {
        this.pendingQuiz = null;
        this.wrongAttempts = 0;
    }

    /**
     * 오답 후 같은 문제를 다시 제시한 재시도.
     * 새 퀴즈가 아니므로 quizCount 는 늘리지 않고, 대기 중인 퀴즈만 유지한다.
     */
    public void repeatPendingQuiz() {
        this.turnsSinceLastQuiz = 0;
    }

    /** 대기 중인 퀴즈에 오답을 제출함. 누적 횟수를 돌려준다. */
    public int recordWrongAttempt() {
        return ++this.wrongAttempts;
    }

    public void incrementTurnsSinceLastQuiz() {
        this.turnsSinceLastQuiz++;
    }

    /**
     * 이어하기(연장): 퀴즈 한도를 늘리고 완결 상태를 해제해 같은 상황의 대화를 계속한다.
     * 페이싱도 초기화해 연장 직후엔 잠시 대화가 이어진 뒤 퀴즈가 나온다.
     */
    public void extendQuizLimit(int additionalQuizzes) {
        this.quizLimit += additionalQuizzes;
        this.isCompleted = false;
        this.turnsSinceLastQuiz = 0;
    }

    /** 이어하기 시점을 타임라인에 표시 (프론트가 구분선 등으로 렌더링 가능). */
    public void addExtensionMarker() {
        addExtensionMarker(0);
    }

    /** 이어하기 시점 표시 + 그때 차감한 포인트 (이어하기 비용은 컬럼 없이 타임라인에 기록한다). */
    public void addExtensionMarker(int chargedPoints) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "extension");
        event.put("quiz_limit", quizLimit);
        event.put("charged_points", chargedPoints);
        timeline.add(event);
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
