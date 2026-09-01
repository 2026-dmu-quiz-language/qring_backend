package com.qring.qring_backend.service.content;

import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.domain.content.StorySession;
import com.qring.qring_backend.domain.content.StorySessionEntity;
import com.qring.qring_backend.domain.content.StorySessionRepository;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.dto.content.StoryArchiveDetailResponse;
import com.qring.qring_backend.dto.content.StoryArchiveListResponse;
import com.qring.qring_backend.dto.content.StoryArchiveResponse;
import com.qring.qring_backend.dto.content.StoryChatRequest;
import com.qring.qring_backend.dto.content.StoryChatResponse;
import com.qring.qring_backend.dto.content.StoryStartRequest;
import com.qring.qring_backend.dto.content.StoryStartResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private final OpenAiStoryService openAiStoryService;
    private final StoryPointManager pointManager;
    private final StorySessionRepository storySessionRepository;
    private final StorySessionMapper sessionMapper;

    public static final int STORY_GENERATION_COST = 30;

    /** 완결된 스토리를 영구 보관하는 추가 비용. 팀에서 금액 확정 전까지 0. */
    public static final int STORY_ARCHIVE_COST = 0;

    /** 미보관 세션의 만료 기준. 경과 시 메모리와 DB 양쪽에서 제거된다. */
    private static final Duration SESSION_TTL = Duration.ofHours(3);

    // 진행 중 세션의 작업용 캐시 (sessionId -> StorySession). 원본은 DB 에 함께 기록된다.
    private final ConcurrentHashMap<String, StorySession> sessionStore = new ConcurrentHashMap<>();

    /**
     * 1단계: 스토리 세션 시작 및 포인트 차감 (-30pt)
     *
     * 트랜잭션 구조: 포인트 차감은 짧은 독립 트랜잭션(StoryPointManager)으로 먼저 커밋하고,
     * 수 초가 걸리는 OpenAI 호출은 트랜잭션 밖에서 수행한다 (DB 커넥션 점유 방지).
     * OpenAI 또는 세션 저장이 실패하면 차감분을 환불한다.
     */
    public StoryStartResponse startStorySession(Long userId, StoryStartRequest request) {
        purgeExpiredSessions();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. (ID: " + userId + ")"));
        int levelCode = user.getLevelCode() != null ? user.getLevelCode() : 1;

        // 1. 포인트 선 차감 (짧은 트랜잭션, 원자적 — 잔액 부족 시 여기서 거절)
        int remainingPoints = pointManager.deduct(userId, STORY_GENERATION_COST);
        log.info("[InteractiveStory] 사용자(ID: {}) 스토리 세션 시작 포인트 차감 완료: -{} pt (잔액 {} pt)",
                userId, STORY_GENERATION_COST, remainingPoints);

        // 2. 세션 객체 생성
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

        // 3. OpenAI 첫 오프닝 생성 — 트랜잭션 밖. 실패 시 환불 후 원래 오류 전달
        Map<String, Object> openingData;
        try {
            openingData = openAiStoryService.generateOpening(session);
        } catch (RuntimeException e) {
            refundSafely(userId, STORY_GENERATION_COST, sessionId, "오프닝 생성 실패");
            throw e;
        }

        String aiFirstMsg = textOrDefault(openingData.get("ai_message"), "Hello! Nice to meet you.");
        String aiFirstTrans = textOrDefault(openingData.get("translation"), "안녕! 만나서 반가워.");
        session.addAssistantMessage(aiFirstMsg, aiFirstTrans);

        // 4. 진행 중 세션을 DB에 기록 (서버 재시작 대비).
        //    story_session 테이블이 아직 없으면(팀원이 생성 예정) 저장이 생략되고 메모리로만 동작한다.
        persistSessionState(session);

        sessionStore.put(sessionId, session);

        return StoryStartResponse.builder()
                .sessionId(sessionId)
                .characterName(request.getCharacterName())
                .situation(request.getSituationDescription())
                .aiFirstMessage(aiFirstMsg)
                .aiFirstTranslation(aiFirstTrans)
                .userRemainingPoints(remainingPoints)
                .build();
    }

    /**
     * 2단계: 실시간 턴 바이 턴 대화 및 퀴즈 응답
     */
    public StoryChatResponse processChatTurn(Long userId, StoryChatRequest request) {
        String sessionId = request.getSessionId();
        StorySession session = sessionStore.get(sessionId);
        if (session == null) {
            // 서버 재시작 등으로 메모리에 없으면 DB 에서 복원 (진행 중 세션만)
            session = restoreSessionFromDb(sessionId);
        }

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

        // 2. OpenAI 턴 응답 생성 (트랜잭션 밖 — DB 커넥션 미점유)
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
            // 채점 결과를 타임라인에 기록 — 직전 user 메시지가 답안이었다는 표시 (보관 시 함께 저장됨)
            session.addQuizResult(pendingQuiz, request.getUserMessage(), answerResult);
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

        // 6. AI 대사 기록 (타임라인에는 번역까지, 프롬프트 히스토리에는 원문만)
        session.addAssistantMessage(aiMsg, translation);
        if (quiz != null) {
            // 방금 그 AI 대사와 함께 출제된 퀴즈 — 타임라인에서 대사 바로 뒤에 위치
            session.addQuizPresented(quiz);
        }

        if (isCompleted) {
            session.setCompleted(true);
            log.info("[InteractiveStory] 세션 {} 완결 처리 (누적 퀴즈 {}개) - 보관 여부 선택 대기", sessionId, session.getQuizCount());
        }

        // 7. 이번 턴까지의 상태를 DB 에 반영 (실패해도 턴은 성공 — 다음 턴에 재시도)
        persistSessionState(session);

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

    /**
     * 3단계: 완결된 스토리 영구 보관 (추가 포인트 결제).
     * 보관하지 않은 완결 세션은 TTL 경과 시 삭제된다.
     */
    public StoryArchiveResponse archiveStory(Long userId, String sessionId) {
        StorySessionEntity entity = storySessionRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("보관할 스토리 세션을 찾을 수 없습니다. (sessionId: " + sessionId + ")"));

        if (StorySessionEntity.STATUS_ARCHIVED.equals(entity.getStatus())) {
            throw new IllegalStateException("이미 보관된 스토리입니다.");
        }
        if (!Boolean.TRUE.equals(entity.getIsCompleted())) {
            throw new IllegalStateException("완결된 스토리만 보관할 수 있습니다. 대화를 끝까지 진행해주세요.");
        }

        // 보관 비용 차감 (0 이면 차감 없이 잔액만 조회)
        int remainingPoints = pointManager.deduct(userId, STORY_ARCHIVE_COST);

        try {
            entity.setStatus(StorySessionEntity.STATUS_ARCHIVED);
            entity.setArchivedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            storySessionRepository.save(entity);
        } catch (RuntimeException e) {
            refundSafely(userId, STORY_ARCHIVE_COST, sessionId, "보관 처리 실패");
            throw e;
        }

        sessionStore.remove(sessionId);
        log.info("[InteractiveStory] 세션 {} 보관 완료 - userId: {}, 결제 {} pt", sessionId, userId, STORY_ARCHIVE_COST);

        return StoryArchiveResponse.builder()
                .sessionId(sessionId)
                .userRemainingPoints(remainingPoints)
                .build();
    }

    /** 보관하지 않기로 선택한 세션 즉시 삭제. */
    public void discardStory(Long userId, String sessionId) {
        StorySessionEntity entity = storySessionRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 스토리 세션을 찾을 수 없습니다. (sessionId: " + sessionId + ")"));

        if (StorySessionEntity.STATUS_ARCHIVED.equals(entity.getStatus())) {
            throw new IllegalStateException("이미 보관된 스토리는 삭제할 수 없습니다.");
        }

        storySessionRepository.delete(entity);
        sessionStore.remove(sessionId);
        log.info("[InteractiveStory] 세션 {} 사용자 선택으로 삭제 - userId: {}", sessionId, userId);
    }

    /** 보관된 스토리 목록. */
    public StoryArchiveListResponse getArchives(Long userId) {
        List<StoryArchiveListResponse.ArchiveSummary> summaries = storySessionRepository
                .findByUserIdAndStatusOrderByArchivedAtDesc(userId, StorySessionEntity.STATUS_ARCHIVED)
                .stream()
                .map(e -> StoryArchiveListResponse.ArchiveSummary.builder()
                        .sessionId(e.getSessionId())
                        .characterName(e.getCharacterName())
                        .situation(e.getSituationDescription())
                        .quizCount(e.getQuizCount())
                        .archivedAt(e.getArchivedAt())
                        .build())
                .toList();
        return StoryArchiveListResponse.builder().archives(summaries).build();
    }

    /** 보관된 스토리 상세 (다시 읽기). */
    public StoryArchiveDetailResponse getArchiveDetail(Long userId, String sessionId) {
        StorySessionEntity entity = storySessionRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("보관된 스토리를 찾을 수 없습니다. (sessionId: " + sessionId + ")"));

        if (!StorySessionEntity.STATUS_ARCHIVED.equals(entity.getStatus())) {
            throw new IllegalArgumentException("보관된 스토리가 아닙니다. (sessionId: " + sessionId + ")");
        }

        return StoryArchiveDetailResponse.builder()
                .sessionId(entity.getSessionId())
                .characterName(entity.getCharacterName())
                .situation(entity.getSituationDescription())
                .tone(entity.getTone())
                .targetLanguage(entity.getTargetLanguage())
                .quizCount(entity.getQuizCount())
                .archivedAt(entity.getArchivedAt())
                .timeline(sessionMapper.parseTimeline(entity.getTimeline()))
                .build();
    }

    /** 메모리에 없는 세션을 DB 에서 복원 (서버 재시작 후 이어하기). */
    private StorySession restoreSessionFromDb(String sessionId) {
        try {
            return storySessionRepository.findById(sessionId)
                    .filter(e -> StorySessionEntity.STATUS_IN_PROGRESS.equals(e.getStatus()))
                    .map(entity -> {
                        StorySession restored = sessionMapper.toDomain(entity);
                        sessionStore.put(sessionId, restored);
                        log.info("[InteractiveStory] 세션 {} DB 에서 복원 (서버 재시작 후 이어하기)", sessionId);
                        return restored;
                    })
                    .orElse(null);
        } catch (Exception e) {
            log.warn("[InteractiveStory] 세션 복원 실패 - sessionId: {}, error: {}", sessionId, e.getMessage());
            return null;
        }
    }

    /** 이번 턴까지의 세션 상태를 DB 에 반영. 실패해도 턴 응답은 성공시킨다 (메모리가 최신, 다음 턴에 재시도). */
    private void persistSessionState(StorySession session) {
        try {
            StorySessionEntity entity = storySessionRepository.findById(session.getSessionId())
                    .orElseGet(() -> sessionMapper.toNewEntity(session));
            sessionMapper.applyState(entity, session);
            storySessionRepository.save(entity);
        } catch (Exception e) {
            log.warn("[InteractiveStory] 세션 DB 저장 실패 (다음 턴에 재시도) - sessionId: {}, error: {}",
                    session.getSessionId(), e.getMessage());
        }
    }

    /** 환불 시도. 환불마저 실패하면 수동 복구가 가능하도록 상세 로그를 남긴다 (원래 오류 전달은 호출부 몫). */
    private void refundSafely(Long userId, int amount, String sessionId, String reason) {
        try {
            pointManager.refund(userId, amount);
        } catch (Exception refundError) {
            log.error("[InteractiveStory] !! 포인트 환불 실패 - 수동 복구 필요 !! userId: {}, amount: {} pt, sessionId: {}, 사유: {}",
                    userId, amount, sessionId, reason, refundError);
        }
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

    /** TTL 을 넘긴 미보관 세션을 메모리와 DB 양쪽에서 제거한다. */
    private void purgeExpiredSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minus(SESSION_TTL);

        sessionStore.values().removeIf(session -> {
            boolean expired = session.getCreatedAt() != null && session.getCreatedAt().isBefore(cutoff);
            if (expired) {
                log.debug("[InteractiveStory] 만료 세션 메모리 제거: {}", session.getSessionId());
            }
            return expired;
        });

        try {
            int removed = storySessionRepository.deleteExpired(StorySessionEntity.STATUS_IN_PROGRESS, cutoff);
            if (removed > 0) {
                log.info("[InteractiveStory] 만료 미보관 세션 {}건 DB 정리", removed);
            }
        } catch (Exception e) {
            log.warn("[InteractiveStory] 만료 세션 DB 정리 실패: {}", e.getMessage());
        }
    }
}
