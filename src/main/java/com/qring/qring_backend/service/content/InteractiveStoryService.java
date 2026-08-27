package com.qring.qring_backend.service.content;

import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.domain.content.StorySession;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.domain.user.UserAsset;
import com.qring.qring_backend.domain.user.UserAssetRepository;
import com.qring.qring_backend.dto.content.StoryChatRequest;
import com.qring.qring_backend.dto.content.StoryChatResponse;
import com.qring.qring_backend.dto.content.StoryStartRequest;
import com.qring.qring_backend.dto.content.StoryStartResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class InteractiveStoryService {

    private final UserRepository userRepository;
    private final UserAssetRepository userAssetRepository;
    private final OpenAiStoryService openAiStoryService;

    public static final int STORY_GENERATION_COST = 30;

    /** 방치된 세션이 메모리에 영구히 쌓이지 않도록 하는 만료 기준. */
    private static final Duration SESSION_TTL = Duration.ofHours(3);

    // 실시간 세션 메모리 스토어 (sessionId -> StorySession)
    private final ConcurrentHashMap<String, StorySession> sessionStore = new ConcurrentHashMap<>();

    /**
     * 1단계: 스토리 세션 시작 및 포인트 차감 (-30pt)
     */
    @Transactional
    public StoryStartResponse startStorySession(Long userId, StoryStartRequest request) {
        purgeExpiredSessions();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. (ID: " + userId + ")"));

        UserAsset asset = userAssetRepository.findByUserUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 자산 정보(포인트)를 찾을 수 없습니다."));

        // 1. 포인트 검증
        int currentPoints = asset.getCurrentPoints() != null ? asset.getCurrentPoints() : 0;
        if (currentPoints < STORY_GENERATION_COST) {
            throw new IllegalStateException(String.format("포인트가 부족합니다. (필요: %d pt, 보유: %d pt)",
                    STORY_GENERATION_COST, currentPoints));
        }

        // 2. 포인트 차감
        int updatedPoints = currentPoints - STORY_GENERATION_COST;
        asset.setCurrentPoints(updatedPoints);
        userAssetRepository.save(asset);
        log.info("[InteractiveStory] 사용자(ID: {}) 스토리 세션 시작 포인트 차감 완료: {} pt -> {} pt",
                userId, currentPoints, updatedPoints);

        // 3. 세션 객체 생성 (levelCode 미설정 사용자는 1로 간주)
        int levelCode = user.getLevelCode() != null ? user.getLevelCode() : 1;
        String sessionId = "sess-" + UUID.randomUUID();
        StorySession session = StorySession.builder()
                .sessionId(sessionId)
                .userId(userId)
                .characterName(request.getCharacterName())
                .situationDescription(request.getSituationDescription())
                .tone(request.getTone())
                .targetLanguage(request.getTargetLanguage() != null ? request.getTargetLanguage() : "English")
                .levelCode(levelCode)
                .build();

        // 4. OpenAI 첫 오프닝 생성 (정규화된 세션 값 기준)
        Map<String, Object> openingData = openAiStoryService.generateOpening(session);
        String aiFirstMsg = textOrDefault(openingData.get("ai_message"), "Hello! Nice to meet you.");
        String aiFirstTrans = textOrDefault(openingData.get("translation"), "안녕! 만나서 반가워.");

        session.addMessage("assistant", aiFirstMsg);
        sessionStore.put(sessionId, session);

        return StoryStartResponse.builder()
                .sessionId(sessionId)
                .characterName(request.getCharacterName())
                .situation(request.getSituationDescription())
                .aiFirstMessage(aiFirstMsg)
                .aiFirstTranslation(aiFirstTrans)
                .userRemainingPoints(updatedPoints)
                .build();
    }

    /**
     * 2단계: 실시간 턴 바이 턴 대화 및 퀴즈 응답
     */
    public StoryChatResponse processChatTurn(Long userId, StoryChatRequest request) {
        String sessionId = request.getSessionId();
        StorySession session = sessionStore.get(sessionId);

        if (session == null) {
            throw new IllegalArgumentException("존재하지 않거나 만료된 스토리 세션입니다. (sessionId: " + sessionId + ")");
        }
        if (!session.getUserId().equals(userId)) {
            log.warn("[InteractiveStory] 세션 소유자 불일치 - 요청 userId: {}, 세션 소유자: {}, sessionId: {}",
                    userId, session.getUserId(), sessionId);
            throw new IllegalArgumentException("해당 스토리 세션에 접근할 권한이 없습니다.");
        }
        if (session.isCompleted()) {
            throw new IllegalStateException("이미 종료된 스토리 세션입니다. (sessionId: " + sessionId + ")");
        }

        // 1. 사용자 메시지 기록
        session.addMessage("user", request.getUserMessage());
        session.incrementTurnsSinceLastQuiz();

        // 2. OpenAI 턴 응답 생성 (사용자 대답 반영 + 실시간 피드백/퀴즈 여부)
        //    호출이 실패하면 이번 턴에 반영한 상태를 되돌려 재시도해도 진행도가 어긋나지 않게 한다.
        Map<String, Object> turnResponse;
        try {
            turnResponse = openAiStoryService.generateTurnResponse(session, request.getUserMessage());
        } catch (RuntimeException e) {
            session.rollbackUserTurn();
            throw e;
        }

        String aiMsg = textOrDefault(turnResponse.get("ai_message"), "Got it!");
        String translation = textOrDefault(turnResponse.get("translation"), "알겠어!");
        boolean isCompleted = toBoolean(turnResponse.get("is_completed"));

        @SuppressWarnings("unchecked")
        Map<String, Object> quiz = turnResponse.get("quiz") instanceof Map
                ? (Map<String, Object>) turnResponse.get("quiz")
                : null;

        // 3. 퀴즈 채택 여부는 서버가 최종 결정한다.
        //    (모델이 is_quiz=true 를 주고도 quiz 를 빠뜨리거나, 5개 상한을 넘겨 출제하는 경우 방지)
        Map<String, Object> pendingQuiz = session.getPendingQuiz();
        boolean modelWantsQuiz = toBoolean(turnResponse.get("is_quiz")) && quiz != null;

        // 오답 후 같은 문제를 다시 낸 재시도는 새 퀴즈가 아니다. 5개 한도를 소모해서는 안 된다.
        boolean isRetry = modelWantsQuiz && isSameQuestion(pendingQuiz, quiz);
        boolean isNewQuiz = modelWantsQuiz && !isRetry
                && session.getQuizCount() < OpenAiStoryService.MAX_QUIZ_COUNT;

        boolean isQuiz = isRetry || isNewQuiz;
        if (!isQuiz) {
            quiz = null;
        }

        // 4. 직전 퀴즈 채점: 서버가 확정할 수 있으면 서버 판정이 최종이고,
        //    확정 불가한 경우(주관식 목록 밖 답안)에만 모델 판정을 쓴다.
        String answerResult = "none";
        if (pendingQuiz != null) {
            String serverVerdict = OpenAiStoryService.gradeAnswer(pendingQuiz, request.getUserMessage());
            answerResult = serverVerdict != null
                    ? serverVerdict
                    : normalizeAnswerResult(turnResponse.get("answer_result"));
        }

        if (isNewQuiz) {
            // 기출 금지 목록에는 짧은 핵심 표현을 담는다 (question 전문이 들어가면 모델이 인식하지 못함)
            String subject = textOrDefault(quiz.get("correct_answer"), null);
            if (subject == null && quiz.get("acceptable_answers") instanceof List<?> l && !l.isEmpty()) {
                subject = textOrDefault(l.get(0), null);
            }
            if (subject == null) {
                subject = textOrDefault(quiz.get("question"), null);
            }
            session.addTestedQuizSubject(subject);
            session.recordQuiz(quiz);
        } else if (isRetry) {
            session.repeatPendingQuiz();
            log.info("[InteractiveStory] 세션 {} 오답 재시도 - 퀴즈 한도 미소모 (누적 {}개)",
                    sessionId, session.getQuizCount());
        } else {
            session.clearPendingQuiz();
        }

        // 5. 퀴즈를 모두 소진하고 마지막 채점까지 끝나면 서버가 종료를 확정한다.
        //    (모델이 is_completed 지시를 장기간 무시하는 사례가 실측에서 확인됨)
        if (!isCompleted && shouldForceComplete(session)) {
            isCompleted = true;
            log.info("[InteractiveStory] 세션 {} 서버 강제 완결 (퀴즈 {}개 채점 완료)", sessionId, session.getQuizCount());
        }

        // 6. AI 대사 히스토리에 추가
        session.addMessage("assistant", aiMsg);

        if (isCompleted) {
            session.setCompleted(true);
            log.info("[InteractiveStory] 세션 {} 완결 처리 (누적 퀴즈 {}개)", sessionId, session.getQuizCount());
        }

        return StoryChatResponse.builder()
                .sessionId(sessionId)
                .aiMessage(aiMsg)
                .translation(translation)
                .isQuiz(isQuiz)
                .quiz(quiz)
                .answerResult(answerResult)
                .currentQuizCount(session.getQuizCount())
                .isCompleted(session.isCompleted())
                .build();
    }

    /** 퀴즈를 모두 소진했고 마지막 퀴즈의 채점까지 끝났는지 (서버 강제 종료 조건). */
    static boolean shouldForceComplete(StorySession session) {
        return session.getQuizCount() >= OpenAiStoryService.MAX_QUIZ_COUNT
                && session.getPendingQuiz() == null;
    }

    /** 직전에 출제된 퀴즈와 같은 문제인지 (오답 재시도 판별). */
    static boolean isSameQuestion(Map<String, Object> pendingQuiz, Map<String, Object> newQuiz) {
        if (pendingQuiz == null || newQuiz == null) {
            return false;
        }
        String before = normalizeQuestion(pendingQuiz.get("question"));
        String after = normalizeQuestion(newQuiz.get("question"));
        return !before.isEmpty() && before.equals(after);
    }

    private static String normalizeQuestion(Object value) {
        return value == null ? "" : String.valueOf(value).replaceAll("\\s+", " ").trim().toLowerCase();
    }

    /** OpenAI 응답 필드가 없거나 null 인 경우까지 안전하게 문자열로 변환. */
    private static String textOrDefault(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? defaultValue : text;
    }

    /** 모델이 boolean 대신 "true"/"false" 문자열을 주더라도 깨지지 않게 변환. */
    private static boolean toBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        return value instanceof String str && Boolean.parseBoolean(str.trim());
    }

    private static String normalizeAnswerResult(Object value) {
        String result = textOrDefault(value, "none").toLowerCase();
        return ("correct".equals(result) || "incorrect".equals(result)) ? result : "none";
    }

    /** TTL 을 넘긴 세션을 메모리에서 제거한다. */
    private void purgeExpiredSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minus(SESSION_TTL);
        sessionStore.values().removeIf(session -> {
            boolean expired = session.getCreatedAt() != null && session.getCreatedAt().isBefore(cutoff);
            if (expired) {
                log.debug("[InteractiveStory] 만료 세션 제거: {}", session.getSessionId());
            }
            return expired;
        });
    }
}
