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
        boolean isQuiz = toBoolean(turnResponse.get("is_quiz"))
                && quiz != null
                && session.getQuizCount() < OpenAiStoryService.MAX_QUIZ_COUNT;
        if (!isQuiz) {
            quiz = null;
        }

        // 4. 직전 퀴즈에 대한 채점 결과 (대기 중인 퀴즈가 없었다면 항상 none)
        String answerResult = session.getPendingQuiz() != null
                ? normalizeAnswerResult(turnResponse.get("answer_result"))
                : "none";

        if (isQuiz) {
            String subject = textOrDefault(quiz.get("correct_answer"), null);
            if (subject == null) {
                subject = textOrDefault(quiz.get("question"), null);
            }
            session.addTestedQuizSubject(subject);
            session.recordQuiz(quiz);
        } else {
            session.clearPendingQuiz();
        }

        // 5. AI 대사 히스토리에 추가
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
