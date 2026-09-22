package com.qring.qring_backend.service.content;

import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.domain.content.StorySession;
import com.qring.qring_backend.domain.content.StorySessionEntity;
import com.qring.qring_backend.domain.content.StorySessionRepository;
import com.qring.qring_backend.domain.user.LearningLanguage;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.domain.user.UserAssetHistory.SourceType;
import com.qring.qring_backend.dto.content.StoryArchiveDetailResponse;
import com.qring.qring_backend.dto.content.StoryArchiveListResponse;
import com.qring.qring_backend.dto.content.StoryArchiveResponse;
import com.qring.qring_backend.dto.content.StoryChatRequest;
import com.qring.qring_backend.dto.content.StoryExtendResponse;
import com.qring.qring_backend.dto.content.StoryResumeResponse;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class InteractiveStoryService {

    private final UserRepository userRepository;
    private final OpenAiStoryService openAiStoryService;
    private final StoryPointManager pointManager;
    private final StorySessionRepository storySessionRepository;
    private final StorySessionMapper sessionMapper;
    private final StoryModelTier modelTiers;

    /** 완결된 스토리를 영구 보관하는 추가 비용. 팀에서 금액 확정 전까지 0. */
    public static final int STORY_ARCHIVE_COST = 0;

    /** 이어하기 1회당 추가되는 퀴즈 개수. */
    public static final int EXTEND_QUIZ_COUNT = 5;

    /**
     * 이어하기 최대 횟수. AI 컨텍스트가 80메시지이고 5퀴즈 구간이 실측 약 25메시지이므로,
     * 2회 연장(총 퀴즈 15개 ≈ 77메시지)까지는 AI가 이야기 전체를 기억하지만 3회부터는
     * 첫 구간이 통째로 컨텍스트 밖으로 밀려난다.
     */
    public static final int MAX_EXTEND_COUNT = 2;

    /** 세션이 가질 수 있는 최대 퀴즈 한도 (기본 5 + 연장 2회 x 5 = 15). */
    public static final int MAX_QUIZ_LIMIT =
            StorySession.DEFAULT_QUIZ_LIMIT + EXTEND_QUIZ_COUNT * MAX_EXTEND_COUNT;

    /** 미보관 세션의 만료 기준. 경과 시 메모리와 DB 양쪽에서 제거된다. */
    private static final Duration SESSION_TTL = Duration.ofHours(3);

    // 진행 중 세션의 작업용 캐시 (sessionId -> StorySession). 원본은 DB 에 함께 기록된다.
    private final ConcurrentHashMap<String, StorySession> sessionStore = new ConcurrentHashMap<>();

    /** 지금 AI 응답을 생성 중인 세션 (sessionId -> 완료 래치). 이어하기가 이 턴을 기다리는 데 쓴다. */
    private final ConcurrentHashMap<String, CountDownLatch> turnsInFlight = new ConcurrentHashMap<>();

    /** 이어하기가 생성 중인 턴을 기다려 주는 최대 시간. 재생성까지 포함한 한 턴이 보통 이 안에 끝난다. */
    private static final Duration RESUME_WAIT_FOR_TURN = Duration.ofSeconds(15);

    /**
     * 1단계: 스토리 세션 시작 및 포인트 차감 (-30pt)
     *
     * 트랜잭션 구조: 포인트 차감은 짧은 독립 트랜잭션(StoryPointManager)으로 먼저 커밋하고,
     * 수 초가 걸리는 OpenAI 호출은 트랜잭션 밖에서 수행한다 (DB 커넥션 점유 방지).
     * OpenAI 또는 세션 저장이 실패하면 차감분을 환불한다.
     */
    @SuppressWarnings("deprecation") // request.getTargetLanguage() 는 무시할 값이지만 로그를 위해 읽는다
    public StoryStartResponse startStorySession(Long userId, StoryStartRequest request) {
        purgeExpiredSessions();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. (ID: " + userId + ")"));
        int levelCode = user.getLevelCode() != null ? user.getLevelCode() : 1;
        // 학습 언어는 토큰의 사용자 설정(users.language)으로 정한다. 요청의 targetLanguage 는 무시 (2026-09-22)
        String targetLanguage = resolveTargetLanguage(user, request.getTargetLanguage());

        // 0. 모델 티어 결정 (잘못된 값은 400). 이어하기에서도 바뀌지 않는다.
        String modelTier = StoryModelTier.normalize(request.getModelTier());
        int startCost = modelTiers.startCostFor(modelTier);

        // 세션 id 를 먼저 정해 두어야 차감 히스토리가 이 세션을 가리킬 수 있다
        String sessionId = "sess-" + UUID.randomUUID();

        // 1. 포인트 선 차감 (짧은 트랜잭션, 원자적 — 잔액 부족 시 여기서 거절)
        int remainingPoints = pointManager.deduct(userId, startCost, SourceType.INTERACTIVE_STORY_CREATE, sessionId);
        log.info("[InteractiveStory] 사용자(ID: {}) 스토리 세션 시작 포인트 차감 완료: -{} pt (잔액 {} pt, 티어 {}, 모델 {}, 언어 {})",
                userId, startCost, remainingPoints, modelTier, modelTiers.modelFor(modelTier), targetLanguage);

        // 2. 세션 객체 생성
        StorySession session = StorySession.builder()
                .sessionId(sessionId)
                .userId(userId)
                .characterName(request.getCharacterName())
                .situationDescription(request.getSituationDescription())
                .tone(request.getTone())
                .targetLanguage(targetLanguage)
                .levelCode(levelCode)
                .modelTier(modelTier)
                .build();

        // 3. OpenAI 첫 오프닝 생성 — 트랜잭션 밖. 실패 시 환불 후 원래 오류 전달
        //    (프리미엄 호출이 실패해도 기본 모델로 조용히 내려가지 않는다 — 돈을 더 냈는데 다른 모델을 쓰는 상황 방지)
        Map<String, Object> openingData;
        try {
            openingData = openAiStoryService.generateOpening(session);
        } catch (RuntimeException e) {
            refundSafely(userId, startCost, sessionId, "오프닝 생성 실패");
            throw e;
        }

        String aiFirstMsg = textOrDefault(openingData.get("ai_message"), "Hello! Nice to meet you.");
        String aiFirstTrans = textOrDefault(openingData.get("translation"), "안녕! 만나서 반가워.");
        session.addAssistantMessage(aiFirstMsg, aiFirstTrans);

        // 오프닝 번역의 말투(반말/존댓말)를 세션에 고정 — 이후 모든 턴이 같은 말투를 유지하도록 프롬프트에 박는다
        session.setSpeechLevel(OpenAiStoryService.detectSpeechLevel(aiFirstTrans));
        log.info("[InteractiveStory] 세션 {} 말투 고정: {}", sessionId, session.getSpeechLevel());

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
                .modelTier(modelTier)
                .chargedPoints(startCost)
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

        // 이 턴이 끝날 때까지 이어하기(/story/resume)가 기다릴 수 있도록 표시해 둔다 (2026-09-22).
        // 생성 중에 앱을 나갔다 들어오면, 표시가 없을 때는 답 없는 내 메시지로 끝난 대화가 내려가고
        // 완성된 응답은 끊긴 요청과 함께 사라졌다.
        CountDownLatch turnLatch = beginTurn(sessionId);
        try {
            // 1. 사용자 메시지 기록. 띄어쓰기 없는 언어에서 타일을 공백으로 이어 보낸 답안은
            //    프롬프트 사본에서 공백을 뗀다 — 그대로 두면 모델이 "어순이 이상하다"고 오해한다 (2026-09-22).
            String userMessage = request.getUserMessage();
            String promptMessage = OpenAiStoryService.normalizeSubmissionForPrompt(session, userMessage);
            session.addMessage("user", userMessage, promptMessage);
            session.incrementTurnsSinceLastQuiz();

            // 1-2. AI 응답 대기 중임을 DB 에 표시 — 응답 전에 서버가 죽으면 복원 시 이 표시로 감지한다
            markPendingUserMessage(sessionId, userMessage);

            // 2. OpenAI 턴 응답 생성 (트랜잭션 밖 — DB 커넥션 미점유)
            //    호출이 실패하면 이번 턴에 반영한 상태를 되돌려 재시도해도 진행도가 어긋나지 않게 한다.
            Map<String, Object> turnResponse;
            try {
                turnResponse = openAiStoryService.generateTurnResponse(session, promptMessage);
            } catch (RuntimeException e) {
                session.rollbackUserTurn();
                clearPendingUserMessage(sessionId); // 실패를 인지하고 응답했으므로 "끊김"이 아니다
                throw e;
            }

            // 모델이 빈 JSON 을 돌려주는 턴이 있다 (실측 2026-09-22: 같은 퀴즈 오답 재시도에서 세 번 연속).
            // 기본 문구("Got it!")를 일본어 스토리에 내보내는 대신, 퀴즈 없는 일반 턴으로 한 번 더 받아 본다.
            if (textOrDefault(turnResponse.get("ai_message"), "").isBlank()) {
                log.warn("[InteractiveStory] 세션 {} 턴 응답에 ai_message 가 없음 - 퀴즈 없는 일반 턴으로 재시도", sessionId);
                turnResponse = recoverEmptyTurn(session, sessionId, promptMessage, turnResponse);
            }

            String aiMsg = stripHangulIfNeeded(session, sessionId,
                    OpenAiStoryService.removeDuplicateTrailingQuestion(
                            textOrDefault(turnResponse.get("ai_message"), ""), session.getTargetLanguage()));
            rememberStorySoFar(session, turnResponse);
            // 번역이 비면 빈 문자열로 둔다 — 엉뚱한 기본 문구("알겠어!")가 영어 대사 밑에 붙지 않게
            String translation = textOrDefault(turnResponse.get("translation"), "");
            if (translation.isEmpty()) {
                log.warn("[InteractiveStory] 세션 {} 턴 응답에 translation 누락", sessionId);
            }
            boolean isCompleted = toBoolean(turnResponse.get("is_completed"));

            @SuppressWarnings("unchecked")
            Map<String, Object> modelQuiz = turnResponse.get("quiz") instanceof Map
                    ? OpenAiStoryService.sanitizeQuiz((Map<String, Object>) turnResponse.get("quiz"), session.getTargetLanguage())
                    : null;
            boolean modelWantsQuiz = toBoolean(turnResponse.get("is_quiz")) && modelQuiz != null;

            // 3. 직전 퀴즈 채점. 서버가 확정할 수 있으면 서버 판정이 최종이고,
            //    확정 불가한 경우(주관식 목록 밖 답안)에만 모델 판정을 쓴다.
            //    정답이 아니면 서버가 "원본" 대기 퀴즈를 그대로 다시 내려보낸다 (모델이 재출제하든 말든, 무엇을 만들든 무시).
            //    오답이 MAX_WRONG_ATTEMPTS 회 쌓이면 정답을 알려주고 넘어간다.
            Map<String, Object> pendingQuiz = session.getPendingQuiz();
            Map<String, Object> quiz = null;
            boolean isRetry = false;
            String answerResult = "none";
            if (pendingQuiz != null) {
                String verdict = OpenAiStoryService.serverVerdict(session, userMessage);
                if (verdict == null) {
                    verdict = normalizeModelVerdict(turnResponse.get("answer_result"));
                }

                if ("correct".equals(verdict)) {
                    answerResult = "correct";
                    session.addQuizResult(pendingQuiz, userMessage, answerResult);
                    session.clearPendingQuiz();
                } else if ("incorrect".equals(verdict)) {
                    answerResult = "incorrect";
                    session.addQuizResult(pendingQuiz, userMessage, answerResult);
                    int wrongAttempts = session.recordWrongAttempt();
                    if (wrongAttempts >= OpenAiStoryService.MAX_WRONG_ATTEMPTS) {
                        log.info("[InteractiveStory] 세션 {} 퀴즈 {}회 오답 - 정답 공개 후 진행 (누적 {}개)",
                                sessionId, wrongAttempts, session.getQuizCount());
                        String revealAnswer = textOrDefault(pendingQuiz.get("correct_answer"), "");
                        if (!revealAnswer.isEmpty() && !OpenAiStoryService.messageContainsAnswer(session.getTargetLanguage(), aiMsg, revealAnswer)) {
                            log.warn("[InteractiveStory] 세션 {} 3회 오답 공개 턴인데 대사에 정답(\"{}\")이 없음", sessionId, revealAnswer);
                        }
                        session.clearPendingQuiz();
                    } else {
                        quiz = pendingQuiz;
                        isRetry = true;
                        session.repeatPendingQuiz();
                        log.info("[InteractiveStory] 세션 {} 오답 재시도 {}/{} - 퀴즈 한도 미소모 (누적 {}개)",
                                sessionId, wrongAttempts, OpenAiStoryService.MAX_WRONG_ATTEMPTS, session.getQuizCount());
                    }
                } else {
                    // 시도 자체가 아님 (딴 얘기 등): 채점 표시 없이 같은 퀴즈를 다시 보여준다
                    answerResult = "none";
                    quiz = pendingQuiz;
                    isRetry = true;
                    session.repeatPendingQuiz();
                    log.info("[InteractiveStory] 세션 {} 퀴즈 미시도 입력 - 같은 퀴즈 유지: \"{}\"",
                            sessionId, userMessage);
                }
            }

            // 4. 새 퀴즈 채택 여부는 서버가 최종 결정한다. 대기 퀴즈가 남아 있으면 새 퀴즈는 받지 않는다.
            //    (모델이 is_quiz=true 를 주고도 quiz 를 빠뜨리거나, 한도를 넘겨 출제하는 경우 방지)
            boolean isNewQuiz = false;
            // 페이싱은 서버가 강제한다: 마지막 퀴즈(또는 시작) 후 2턴이 지나야 새 퀴즈를 받는다.
            // 3회 오답 공개 턴이나 정답 직후 턴에 모델이 새 퀴즈를 끼워 넣던 실측 사례 방지.
            boolean pacingAllowsQuiz = session.getTurnsSinceLastQuiz() >= OpenAiStoryService.QUIZ_ALLOWED_FROM_TURN
                    && !OpenAiStoryService.isOverdueForClose(session);
            // 퀴즈 턴이 연속으로 실패하면 부드러운 검사를 풀어 준다 (실측: 메타 질문 12턴 연속 거부 → 세션이 안 끝남)
            boolean relaxedChecks = session.getFailedQuizTurns() >= OpenAiStoryService.RELAX_CHECKS_AFTER_FAILED_QUIZ_TURNS;
            if (relaxedChecks) {
                log.info("[InteractiveStory] 세션 {} 퀴즈 턴 {}회 연속 실패 - 구조 검사만 적용", sessionId, session.getFailedQuizTurns());
            }
            if (!isRetry && modelWantsQuiz && !pacingAllowsQuiz) {
                log.info("[InteractiveStory] 세션 {} 페이싱 미달({}턴)인데 모델이 퀴즈를 냄 - 무시", sessionId, session.getTurnsSinceLastQuiz());
            }
            if (!isRetry && modelWantsQuiz && pacingAllowsQuiz && session.getQuizCount() < session.getQuizLimit()) {
                String rejection = rejectionReason(session, aiMsg, modelQuiz, relaxedChecks);
                if (rejection != null) {
                    // 거부된 퀴즈를 그냥 버리면 "질문만 나가고 퀴즈는 없는" 턴이 되고, 다음 턴에 같은 질문이 반복된다 (실측).
                    // 거부 사유를 붙여 한 번 다시 받아 대사와 퀴즈를 함께 교체한다. 답안 판정은 이미 확정된 값을 유지한다.
                    log.warn("[InteractiveStory] 세션 {} 퀴즈 거부 ({}) - 사유를 붙여 1회 재생성: {}",
                            sessionId, rejection, modelQuiz.get("correct_answer"));
                    try {
                        Map<String, Object> redo = openAiStoryService.regenerateTurnWithCorrection(
                                session, promptMessage, rejection, answerResult);
                        String redoMsg = stripHangulIfNeeded(session, sessionId,
                                OpenAiStoryService.removeDuplicateTrailingQuestion(
                                        textOrDefault(redo.get("ai_message"), ""), session.getTargetLanguage()));
                        @SuppressWarnings("unchecked")
                        Map<String, Object> redoQuiz = redo.get("quiz") instanceof Map
                                ? OpenAiStoryService.sanitizeQuiz((Map<String, Object>) redo.get("quiz"), session.getTargetLanguage())
                                : null;
                        boolean redoWantsQuiz = toBoolean(redo.get("is_quiz")) && redoQuiz != null;
                        String redoRejection = redoWantsQuiz ? rejectionReason(session, redoMsg, redoQuiz, relaxedChecks) : "no quiz";
                        if (redoRejection == null) {
                            aiMsg = redoMsg;
                            translation = textOrDefault(redo.get("translation"), "");
                            rememberStorySoFar(session, redo);
                            modelQuiz = redoQuiz;
                            rejection = null;
                        } else {
                            // 두 번 다 거부: 퀴즈용 대사(메타 질문 등)를 내보내지 않고, 이번 턴을 퀴즈 없는 일반 턴으로 다시 받는다
                            log.warn("[InteractiveStory] 세션 {} 재생성 퀴즈도 거부 ({}) - 퀴즈 없는 일반 턴으로 대체", sessionId, redoRejection);
                            Map<String, Object> plain = openAiStoryService.generateNonQuizTurn(session, promptMessage, answerResult);
                            String plainMsg = stripHangulIfNeeded(session, sessionId,
                                    OpenAiStoryService.removeDuplicateTrailingQuestion(
                                            textOrDefault(plain.get("ai_message"), ""), session.getTargetLanguage()));
                            if (!plainMsg.isEmpty()) {
                                aiMsg = plainMsg;
                                translation = textOrDefault(plain.get("translation"), "");
                                rememberStorySoFar(session, plain);
                            }
                        }
                    } catch (RuntimeException e) {
                        log.warn("[InteractiveStory] 세션 {} 퀴즈 재생성/대체 호출 실패 - 첫 대사로 퀴즈 없이 진행: {}", sessionId, e.getMessage());
                    }
                }
                if (rejection != null) {
                    session.setFailedQuizTurns(session.getFailedQuizTurns() + 1);
                }
                if (rejection == null) {
                    isNewQuiz = true;
                    quiz = OpenAiStoryService.ensureQuestionQuotesMeaning(modelQuiz); // "asked" 등 내부 필드는 세션에 남긴다
                    quiz.put("quiz_number", session.getQuizCount() + 1); // 번호는 서버가 매긴다 (모델 번호는 #1, null 등으로 엉킴)
                    aiMsg = OpenAiStoryService.removeDuplicateTrailingQuestion(aiMsg, session.getTargetLanguage()); // 같은 질문 두 번 붙는 사례 정리
                }
            }

            boolean isQuiz = isRetry || isNewQuiz;

            if (isNewQuiz) {
                // 기출 금지 목록에는 정답과 허용 답안 전부를 담는다
                // (question 전문 대신 짧은 핵심 표현 — 형식만 바꾼 재출제도 답안이 겹치면 잡힌다)
                boolean recorded = false;
                String correctAnswer = textOrDefault(quiz.get("correct_answer"), null);
                if (correctAnswer != null) {
                    session.addTestedQuizSubject(correctAnswer);
                    recorded = true;
                }
                if (quiz.get("acceptable_answers") instanceof List<?> l) {
                    for (Object o : l) {
                        String acceptable = textOrDefault(o, null);
                        if (acceptable != null) {
                            session.addTestedQuizSubject(acceptable);
                            recorded = true;
                        }
                    }
                }
                if (!recorded) {
                    session.addTestedQuizSubject(textOrDefault(quiz.get("question"), null));
                }
                session.recordQuiz(quiz);
            }

            // 5. 종료 확정은 서버가 한다: 퀴즈 한도를 모두 채우고 마지막 채점까지 끝났을 때만 완결이다.
            //    - 퀴즈가 남았거나 대기 중인데 모델이 is_completed=true 를 보내는 사례(4개째에 종료 선언) 실측 → 무시
            //    - 모두 끝났는데 모델이 마무리하지 않는 사례 실측 → 강제 종료
            boolean allQuizzesDone = shouldForceComplete(session);
            if (isCompleted && !allQuizzesDone) {
                log.info("[InteractiveStory] 세션 {} 모델의 조기 완결 무시 (퀴즈 {}/{}, 대기 퀴즈 {})",
                        sessionId, session.getQuizCount(), session.getQuizLimit(), session.getPendingQuiz() != null);
            }
            if (!isCompleted && allQuizzesDone) {
                log.info("[InteractiveStory] 세션 {} 서버 강제 완결 (퀴즈 {}개 채점 완료)", sessionId, session.getQuizCount());
            }
            isCompleted = allQuizzesDone;

            // 6. AI 대사 기록 (타임라인에는 번역까지, 프롬프트 히스토리에는 원문만)
            session.addAssistantMessage(aiMsg, translation);
            if (quiz != null) {
                // 방금 그 AI 대사와 함께 출제된 퀴즈 — 타임라인에서 대사 바로 뒤에 위치
                session.addQuizPresented(OpenAiStoryService.clientQuizView(quiz));
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
                    .quiz(OpenAiStoryService.clientQuizView(quiz))
                    .answerResult(answerResult)
                    .currentQuizCount(session.getQuizCount())
                    .quizLimit(session.getQuizLimit())
                    .canExtend(canExtend(session))
                    .isCompleted(session.isCompleted())
                    .build();
        } finally {
            endTurn(sessionId, turnLatch);
        }
    }

    /**
     * 이어하기(연장): 완결된 스토리의 퀴즈 한도를 늘리고, 마무리된 장면을 자연스럽게 다시 열어
     * 같은 상황의 대화를 계속한다. 트랜잭션 구조는 세션 시작과 동일 (선 차감 → OpenAI → 실패 시 환불).
     */
    public StoryExtendResponse extendStory(Long userId, String sessionId) {
        StorySession session = sessionStore.get(sessionId);
        if (session == null) {
            session = restoreSessionFromDb(sessionId);
        }
        if (session == null) {
            throw new IllegalArgumentException("이어할 스토리 세션을 찾을 수 없습니다. (sessionId: " + sessionId + ")");
        }
        if (!session.getUserId().equals(userId)) {
            throw new IllegalArgumentException("해당 스토리 세션에 접근할 권한이 없습니다.");
        }
        if (!session.isCompleted()) {
            throw new IllegalStateException("진행 중인 스토리는 그대로 대화를 계속하면 됩니다. 이어하기는 완결된 스토리에서만 가능합니다.");
        }
        if (!canExtend(session)) {
            throw new IllegalStateException(String.format(
                    "이어하기는 최대 %d회까지 가능합니다. 이 스토리는 이미 한도(퀴즈 %d개)에 도달했습니다.",
                    MAX_EXTEND_COUNT, MAX_QUIZ_LIMIT));
        }

        // 1. 연장 비용 선 차감 (짧은 트랜잭션, 원자적 — 잔액 부족 시 여기서 거절). 티어는 시작 때 것을 따른다 (업그레이드 없음)
        int extendCost = modelTiers.extendCostFor(session.getModelTier());
        int remainingPoints = pointManager.deduct(userId, extendCost, SourceType.INTERACTIVE_STORY_EXTEND, sessionId);

        // 2. 마무리된 장면을 다시 여는 연결 대사 생성 — 트랜잭션 밖. 실패 시 환불, 세션은 완결 상태 그대로
        Map<String, Object> continuation;
        try {
            continuation = openAiStoryService.generateContinuation(session);
        } catch (RuntimeException e) {
            refundSafely(userId, extendCost, sessionId, "이어하기 대사 생성 실패");
            throw e;
        }

        // 3. 한도 확장 + 완결 해제 + 연결 대사 기록
        session.extendQuizLimit(EXTEND_QUIZ_COUNT);
        session.addExtensionMarker(extendCost);

        String aiMsg = textOrDefault(continuation.get("ai_message"), "Wait, before we go - one more thing!");
        String translation = textOrDefault(continuation.get("translation"), "잠깐, 가기 전에 하나만 더!");
        if (continuation.get("translation") == null || String.valueOf(continuation.get("translation")).isBlank()) {
            log.warn("[InteractiveStory] 세션 {} 이어하기 응답에 translation 누락 - 기본 문구로 대체", sessionId);
        }
        aiMsg = stripHangulIfNeeded(session, sessionId, aiMsg);
        session.addAssistantMessage(aiMsg, translation);

        persistSessionState(session);
        log.info("[InteractiveStory] 세션 {} 이어하기 - 퀴즈 한도 {} -> {} (userId: {})",
                sessionId, session.getQuizLimit() - EXTEND_QUIZ_COUNT, session.getQuizLimit(), userId);

        return StoryExtendResponse.builder()
                .sessionId(sessionId)
                .aiMessage(aiMsg)
                .translation(translation)
                .currentQuizCount(session.getQuizCount())
                .quizLimit(session.getQuizLimit())
                .canExtend(canExtend(session))
                .userRemainingPoints(remainingPoints)
                .modelTier(session.getModelTier())
                .chargedPoints(extendCost)
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
        int remainingPoints = pointManager.deduct(userId, STORY_ARCHIVE_COST, SourceType.INTERACTIVE_STORY_ARCHIVE, sessionId);

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
                        .modelTier(sessionMapper.readModelTier(e))
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
                .modelTier(sessionMapper.readModelTier(entity))
                .timeline(sessionMapper.parseTimeline(entity.getTimeline()))
                .build();
    }

    /**
     * 앱 재실행 후 이어하기: 사용자의 가장 최근 진행 중 세션과 전체 타임라인을 반환한다.
     * 세션을 메모리 캐시에 올려두므로 곧바로 이어지는 /story/chat 이 정상 동작한다.
     * 완결됐지만 아직 보관/삭제를 선택하지 않은 세션도 반환된다 (is_completed 로 구분).
     */
    public StoryResumeResponse resumeStory(Long userId) {
        // 진행 중인 세션은 여러 개일 수 있다 (하다가 나가서 새로 만들기를 반복한 경우). 전부 돌려준다.
        List<StorySessionEntity> entities = storySessionRepository
                .findByUserIdAndStatusOrderByUpdatedAtDesc(userId, StorySessionEntity.STATUS_IN_PROGRESS);
        if (entities.isEmpty()) {
            return StoryResumeResponse.builder()
                    .hasSession(false)
                    .turnInProgress(false)
                    .sessions(List.of())
                    .build();
        }

        List<StoryResumeResponse.Session> sessions = entities.stream().map(this::toResumeSession).toList();
        StoryResumeResponse.Session latest = sessions.get(0);

        // 낱개 필드는 가장 최근 세션 값으로 채운다 — 목록이 생기기 전의 앱과 호환을 유지한다
        return StoryResumeResponse.builder()
                .hasSession(true)
                .sessionId(latest.getSessionId())
                .characterName(latest.getCharacterName())
                .situation(latest.getSituation())
                .tone(latest.getTone())
                .targetLanguage(latest.getTargetLanguage())
                .currentQuizCount(latest.getCurrentQuizCount())
                .quizLimit(latest.getQuizLimit())
                .canExtend(latest.getCanExtend())
                .isCompleted(latest.getIsCompleted())
                .modelTier(latest.getModelTier())
                .turnInProgress(latest.getTurnInProgress())
                .timeline(latest.getTimeline())
                .sessions(sessions)
                .build();
    }

    /** 진행 중 세션 하나를 이어하기 응답 형태로 만든다 (생성 중인 턴이 있으면 기다렸다가). */
    private StoryResumeResponse.Session toResumeSession(StorySessionEntity entity) {
        String sessionId = entity.getSessionId();

        // 생성 중인 턴이 있으면 끝날 때까지 기다렸다가 완성된 대화를 돌려준다 (2026-09-22).
        // 기다리지 않으면 답이 없는 내 메시지로 끝난 대화가 내려가고, 그 사이 완성된 AI 응답은
        // 이미 끊긴 요청으로 나가 앱이 영영 받지 못한다 (실측: 중국어 세션 02:35).
        boolean turnInProgress = awaitTurnInFlight(sessionId);

        // 턴이 끝났다면 그동안 DB 가 갱신됐으므로 다시 읽는다
        StorySessionEntity current = turnInProgress
                ? entity
                : storySessionRepository.findById(sessionId).orElse(entity);

        // AI 응답을 받지 못하고 끊긴 턴이 있으면 정리 (해당 메시지는 기록에 없으므로 재전송하면 됨).
        // 아직 생성 중인 턴의 표시는 건드리지 않는다 — 그 턴은 끊긴 것이 아니다.
        if (!turnInProgress && current.getPendingUserMessage() != null) {
            log.warn("[InteractiveStory] 세션 {} - AI 응답을 받지 못하고 끊긴 메시지 감지, 대기 표시 정리: {}",
                    sessionId, current.getPendingUserMessage());
            current.setPendingUserMessage(null);
            storySessionRepository.save(current);
        }

        // 메모리에 살아있는 세션이 있으면 그쪽이 최신이다 (DB 저장이 한 턴 뒤처졌을 수 있음)
        StorySession session = sessionStore.get(sessionId);
        if (session == null) {
            session = sessionMapper.toDomain(current);
            sessionStore.put(sessionId, session);
            log.info("[InteractiveStory] 세션 {} 이어하기 - DB 에서 복원", sessionId);
        }

        return StoryResumeResponse.Session.builder()
                .sessionId(session.getSessionId())
                .characterName(session.getCharacterName())
                .situation(session.getSituationDescription())
                .tone(session.getTone())
                .targetLanguage(session.getTargetLanguage())
                .currentQuizCount(session.getQuizCount())
                .quizLimit(session.getQuizLimit())
                .canExtend(canExtend(session))
                .isCompleted(session.isCompleted())
                .modelTier(session.getModelTier())
                .turnInProgress(turnInProgress)
                .updatedAt(current.getUpdatedAt())
                .timeline(session.getTimeline())
                .build();
    }

    /**
     * 대상 언어가 아닌 한글 문장을 대사에서 떼어낸다. 첫 응답뿐 아니라 재생성·대체·이어하기 대사에도 적용한다
     * (2026-09-22 실측: 재생성 경로에는 적용되지 않아 한국어 질문이 그대로 화면에 나갔다).
     */
    private static String stripHangulIfNeeded(StorySession session, String sessionId, String aiMsg) {
        if (!OpenAiStoryService.hasUnexpectedHangul(session.getTargetLanguage(), aiMsg)) {
            return aiMsg;
        }
        log.warn("[InteractiveStory] 세션 {} AI 대사에 한글 혼입 (대상 언어 {}) - 한글 문장 제거: {}",
                sessionId, session.getTargetLanguage(), aiMsg);
        return OpenAiStoryService.stripHangulSentences(session.getTargetLanguage(), aiMsg);
    }

    /**
     * 모델이 ai_message 없이 응답한 턴의 복구. 퀴즈 없는 일반 턴으로 한 번 더 받아 보고, 그래도 비면 이번 턴을 되돌리고
     * 오류를 돌려준다 — 기본 문구가 스토리에 섞여 나가는 것보다 낫다 (사용자는 같은 메시지를 다시 보내면 된다).
     */
    private Map<String, Object> recoverEmptyTurn(StorySession session, String sessionId, String promptMessage,
                                                 Map<String, Object> original) {
        try {
            Map<String, Object> plain = openAiStoryService.generateNonQuizTurn(session, promptMessage, "none");
            if (!textOrDefault(plain.get("ai_message"), "").isBlank()) {
                return plain;
            }
        } catch (RuntimeException e) {
            log.warn("[InteractiveStory] 세션 {} 빈 응답 복구 호출 실패: {}", sessionId, e.getMessage());
        }
        log.error("[InteractiveStory] 세션 {} 빈 응답을 복구하지 못해 이번 턴을 되돌린다", sessionId);
        session.rollbackUserTurn();
        clearPendingUserMessage(sessionId);
        // IllegalArgumentException 이어야 GlobalExceptionHandler 가 이 문구를 그대로 400 으로 내보낸다
        // (IllegalStateException 은 500 의 일반 문구로 묻힌다)
        throw new IllegalArgumentException("AI 응답을 받지 못했습니다. 잠시 후 같은 메시지를 다시 보내 주세요.");
    }

    /** 이 세션의 턴이 시작됐음을 표시한다. 반환한 래치는 endTurn 에 그대로 넘긴다. */
    private CountDownLatch beginTurn(String sessionId) {
        CountDownLatch latch = new CountDownLatch(1);
        turnsInFlight.put(sessionId, latch);
        return latch;
    }

    /** 턴 종료 표시. 같은 세션에 새 턴이 시작됐으면 그쪽 표시는 건드리지 않는다. */
    private void endTurn(String sessionId, CountDownLatch latch) {
        turnsInFlight.remove(sessionId, latch);
        latch.countDown();
    }

    /** 생성 중인 턴이 있으면 끝날 때까지 기다린다. 시간 안에 끝나지 않으면 true (아직 진행 중). */
    private boolean awaitTurnInFlight(String sessionId) {
        CountDownLatch latch = turnsInFlight.get(sessionId);
        if (latch == null) {
            return false;
        }
        log.info("[InteractiveStory] 세션 {} 이어하기 - 생성 중인 턴을 최대 {}초 기다린다",
                sessionId, RESUME_WAIT_FOR_TURN.toSeconds());
        boolean stillRunning = awaitTurn(latch, RESUME_WAIT_FOR_TURN);
        if (stillRunning) {
            log.warn("[InteractiveStory] 세션 {} 이어하기 - 기다리는 동안 턴이 끝나지 않아 turn_in_progress 로 응답", sessionId);
        }
        return stillRunning;
    }

    /** 래치가 제한 시간 안에 내려가면 false(턴 종료), 시간이 넘으면 true(아직 진행 중). */
    static boolean awaitTurn(CountDownLatch latch, Duration timeout) {
        try {
            return !latch.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return true;
        }
    }

    /** 메모리에 없는 세션을 DB 에서 복원 (서버 재시작 후 이어하기). */
    private StorySession restoreSessionFromDb(String sessionId) {
        try {
            return storySessionRepository.findById(sessionId)
                    .filter(e -> StorySessionEntity.STATUS_IN_PROGRESS.equals(e.getStatus()))
                    .map(entity -> {
                        // AI 응답을 받지 못한 채 끊긴 턴 감지: 해당 사용자 메시지는 저장된 대화에 없으므로
                        // 표시만 정리하고, 사용자가 같은 메시지를 다시 보내면 정상 턴으로 처리된다.
                        if (entity.getPendingUserMessage() != null) {
                            log.warn("[InteractiveStory] 세션 {} - AI 응답을 받지 못하고 끊긴 메시지 감지, 대기 표시 정리: {}",
                                    sessionId, entity.getPendingUserMessage());
                            entity.setPendingUserMessage(null);
                            storySessionRepository.save(entity);
                        }

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

    /** AI 응답 대기 중인 사용자 메시지를 DB 에 표시. 실패해도 턴 진행에는 영향 없음. */
    private void markPendingUserMessage(String sessionId, String userMessage) {
        try {
            storySessionRepository.findById(sessionId).ifPresent(entity -> {
                entity.setPendingUserMessage(sessionMapper.toPendingMessageJson(userMessage));
                storySessionRepository.save(entity);
            });
        } catch (Exception e) {
            log.debug("[InteractiveStory] pending_user_message 기록 생략 - sessionId: {}, error: {}", sessionId, e.getMessage());
        }
    }

    /** AI 응답 대기 표시 해제 (호출 실패를 인지하고 사용자에게 응답한 경우). */
    private void clearPendingUserMessage(String sessionId) {
        try {
            storySessionRepository.findById(sessionId).ifPresent(entity -> {
                if (entity.getPendingUserMessage() != null) {
                    entity.setPendingUserMessage(null);
                    storySessionRepository.save(entity);
                }
            });
        } catch (Exception e) {
            log.debug("[InteractiveStory] pending_user_message 해제 생략 - sessionId: {}, error: {}", sessionId, e.getMessage());
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
            pointManager.refund(userId, amount, sessionId);
        } catch (Exception refundError) {
            log.error("[InteractiveStory] !! 포인트 환불 실패 - 수동 복구 필요 !! userId: {}, amount: {} pt, sessionId: {}, 사유: {}",
                    userId, amount, sessionId, reason, refundError);
        }
    }

    /**
     * 세션의 학습 언어를 정한다 (2026-09-22). 근거는 토큰 사용자의 설정 언어(users.language: EN/JA/ZH)뿐이다.
     * 요청의 targetLanguage 는 프론트가 채울 수 없는 값이라(설계상 없음) 무시한다 — 그동안 늘 기본값 "English" 가 들어가
     * 일본어·중국어 학습자도 영어 스토리를 받던 원인. 언어가 비었거나(온보딩 전) 모르는 코드면 기본값으로 내리고 경고를 남긴다.
     */
    static String resolveTargetLanguage(User user, String requestedLanguage) {
        LearningLanguage language = LearningLanguage.fromCode(user.getLanguage()).orElse(null);
        if (language == null) {
            language = LearningLanguage.DEFAULT;
            log.warn("[InteractiveStory] 사용자(ID: {}) 학습 언어가 비었거나 지원하지 않는 코드({}) - 기본값 {} 로 진행",
                    user.getUserId(), user.getLanguage(), language.getPromptName());
        }
        if (requestedLanguage != null && !requestedLanguage.isBlank()
                && !requestedLanguage.trim().equalsIgnoreCase(language.getPromptName())) {
            log.info("[InteractiveStory] 사용자(ID: {}) 요청의 targetLanguage(\"{}\")는 무시하고 설정 언어 {} 사용",
                    user.getUserId(), requestedLanguage, language.getPromptName());
        }
        return language.getPromptName();
    }

    /** 이 세션이 이어하기를 더 할 수 있는지 (연장 상한: 총 퀴즈 한도 MAX_QUIZ_LIMIT). */
    static boolean canExtend(StorySession session) {
        return session.getQuizLimit() < MAX_QUIZ_LIMIT;
    }

    /** 퀴즈 한도를 모두 소진했고 마지막 퀴즈의 채점까지 끝났는지 (서버 강제 종료 조건). */
    static boolean shouldForceComplete(StorySession session) {
        boolean allQuizzesGraded = session.getQuizCount() >= session.getQuizLimit()
                && session.getPendingQuiz() == null;
        // 마지막 퀴즈 후 턴 수가 상한을 넘으면 퀴즈가 남았어도 닫는다 (무한 대화 방지). 프롬프트도 같은 턴에 마무리를 지시한다.
        return allQuizzesGraded || OpenAiStoryService.isOverdueForClose(session);
    }

    /** 모델이 매 턴 갱신하는 "지금까지의 이야기" 메모를 세션에 저장한다 (비어 있으면 이전 값 유지). */
    private static void rememberStorySoFar(StorySession session, Map<String, Object> response) {
        String note = textOrDefault(response.get("story_so_far"), "");
        if (note.isEmpty()) {
            return;
        }
        // 기억 메모는 한국어여야 다음 턴 프롬프트에서 제 역할을 한다.
        // 실측(2026-09-22): 일본어 세션에서 메모가 한국어·일본어 혼용으로 쌓였다.
        if (OpenAiStoryService.hangulRatio(note) < 0.5) {
            log.warn("[InteractiveStory] 세션 {} story_so_far 가 한국어가 아니어서 이전 메모 유지: \"{}\"",
                    session.getSessionId(), note);
            return;
        }
        session.setStorySoFar(note.length() > 600 ? note.substring(0, 600) : note);
    }

    /**
     * 새 퀴즈가 이미 다뤘던 표현을 다시 묻는지 판별 (형식만 바꾼 재출제 차단).
     * 새 퀴즈의 정답/허용 답안이 기출 표현과 단어 단위로 완전히 일치하거나,
     * 기출 표현을 단어 경계에서 통째로 포함하면서 그 표현이 답안의 거의 전부인 경우
     * (답안 단어 수가 기출 단어 수 + 1 이하, 예: "iced" 기출 → "iced latte") 중복이다.
     * 긴 문장 답안이 기출 단어 하나를 포함하는 경우("relax" 기출 → "I want to relax at home")는
     * 문장 자체를 새로 묻는 것이므로 중복으로 보지 않는다. 부분 문자열 매칭("rest" → "restaurant")도 하지 않는다.
     */
    static boolean isDuplicateQuizSubject(List<String> testedSubjects, Map<String, Object> quiz) {
        return isDuplicateQuizSubject(testedSubjects, quiz, "English");
    }

    /**
     * 띄어쓰기 없는 언어에서는 낱말 비교가 전혀 동작하지 않아 같은 표현이 두 번 출제됐다
     * (실측 2026-09-22: "続けてベットします" 와 "ベットを続けます"). 그 언어는 글자 구성으로 본다.
     */
    static boolean isDuplicateQuizSubject(List<String> testedSubjects, Map<String, Object> quiz, String targetLanguage) {
        if (testedSubjects.isEmpty() || quiz == null) {
            return false;
        }
        if (!LearningLanguage.usesSpaces(targetLanguage)) {
            List<String> answers = new java.util.ArrayList<>();
            String correctAnswer = textOrDefault(quiz.get("correct_answer"), null);
            if (correctAnswer != null) {
                answers.add(correctAnswer);
            }
            if (quiz.get("acceptable_answers") instanceof List<?> list) {
                for (Object o : list) {
                    String acceptable = textOrDefault(o, null);
                    if (acceptable != null) {
                        answers.add(acceptable);
                    }
                }
            }
            for (String tested : testedSubjects) {
                for (String answer : answers) {
                    if (OpenAiStoryService.sameContentNoSpace(tested, answer)) {
                        return true;
                    }
                }
            }
            return false;
        }

        List<List<String>> candidates = new java.util.ArrayList<>();
        String correctAnswer = textOrDefault(quiz.get("correct_answer"), null);
        if (correctAnswer != null) {
            candidates.add(subjectWords(correctAnswer));
        }
        if (quiz.get("acceptable_answers") instanceof List<?> l) {
            for (Object o : l) {
                String acceptable = textOrDefault(o, null);
                if (acceptable != null) {
                    candidates.add(subjectWords(acceptable));
                }
            }
        }
        candidates.removeIf(List::isEmpty);
        if (candidates.isEmpty()) {
            return false;
        }

        for (String tested : testedSubjects) {
            List<String> testedWords = subjectWords(tested);
            if (testedWords.isEmpty()) {
                continue;
            }
            for (List<String> candidate : candidates) {
                if (candidate.equals(testedWords)) {
                    return true;
                }
                if (candidate.size() <= testedWords.size() + 1
                        && java.util.Collections.indexOfSubList(candidate, testedWords) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 소문자화 후 문장부호를 떼고 공백 기준으로 단어를 나눈다 (단어 경계 비교용). */
    private static List<String> subjectWords(String value) {
        if (value == null) {
            return List.of();
        }
        String cleaned = value.toLowerCase().replaceAll("[^\\p{L}\\p{N}\\s']", " ").trim();
        if (cleaned.isEmpty()) {
            return List.of();
        }
        return java.util.Arrays.stream(cleaned.split("\\s+"))
                .filter(w -> !w.isEmpty())
                .toList();
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

    /**
     * 모델이 낸 새 퀴즈를 서버가 받을 수 없는 이유. 받을 수 있으면 null.
     * 재생성 호출 때 모델에게 그대로 전달되므로 영어로 쓴다.
     */
    private static String rejectionReason(StorySession session, String aiMsg, Map<String, Object> quiz) {
        return rejectionReason(session, aiMsg, quiz, false);
    }

    /**
     * @param relaxed 퀴즈 턴이 연속 실패한 뒤에는 true. 구조 검사(기출 중복·앵무새·대사 연결)만 남기고
     *                되묻기·질문 반복·메타·자기질문·설명형 검사는 건너뛴다 (세션이 영원히 안 끝나는 것보다 낫다).
     */
    private static String rejectionReason(StorySession session, String aiMsg, Map<String, Object> quiz, boolean relaxed) {
        String structural = OpenAiStoryService.structuralProblem(quiz, session.getTargetLanguage());
        if (structural != null) {
            // 띄어쓰기 없는 언어에서 서버가 고칠 수 없는 퀴즈 (타일이 정답을 못 이룸, 주관식이 너무 긺). 구조 검사라 relaxed 여도 유지
            return structural;
        }
        // --- 아래 세 가지는 "학습자가 답할 방법이 없는 퀴즈"라 relaxed 여도 항상 거부한다 (팀 결정 2026-09-22) ---
        if (!OpenAiStoryService.hasHangul(textOrDefault(quiz.get("question"), ""))) {
            // sanitizeQuiz 가 reply_meaning 으로 다시 써 주므로, 여기까지 오면 reply_meaning 도 한국어가 아니라는 뜻이다.
            // 실측(2026-09-22)에서 모델이 question 을 통째로 빠뜨려 네 턴이 날아갔다 — 무엇이 없는지 정확히 알려 준다
            return "the quiz has no usable Korean \"question\". Fill in EVERY field of the quiz object: "
                    + "\"reply_meaning\" is the Korean line the learner would speak, and \"question\" is the Korean "
                    + "sentence built from it (\"'<reply_meaning>'를 뜻하는 표현은?\" / \"'<reply_meaning>'가 되도록 "
                    + "단어를 배열해 보세요.\"). Without it the learner has no idea what is being asked";
        }
        if (!OpenAiStoryService.hasHangul(textOrDefault(quiz.get("reply_meaning"), ""))) {
            return "\"reply_meaning\" (\"" + quiz.get("reply_meaning") + "\") is missing or not written in Korean. "
                    + "It must be the Korean line the learner would speak in reply";
        }
        if (OpenAiStoryService.isQuestionAboutAiItself(quiz)) {
            // 학습자가 AI 자신의 행동·감정·이름을 대신 말하게 하는 퀴즈 (실측: "내가 자주 쓰는 필살기가 뭐라고 생각해?")
            return "your question (\"" + quiz.get("asked") + "\") is about YOUR OWN action, feeling, plan or name, so the "
                    + "learner would have to read your mind. Ask what THEY will do, choose, feel, or want instead";
        }
        String wrongScript = OpenAiStoryService.wrongScriptField(session.getTargetLanguage(), quiz);
        if (wrongScript != null) {
            // "asked" 가 한국어나 영어면 그 문장이 대사 끝에 끌려 들어가고, 정답·보기·타일이 다른 문자로 오면 풀 수 없는 문제가 된다
            return "\"" + wrongScript + "\" (\"" + quiz.get(wrongScript) + "\") is not written in "
                    + session.getTargetLanguage() + ". Everything you SAY and everything the learner reads as the "
                    + "answer - asked, correct_answer, options, tiles, acceptable_answers - is in "
                    + session.getTargetLanguage() + ". Only question, hint, explanation and reply_meaning are Korean";
        }
        String shownAnswer = OpenAiStoryService.answerEchoedInQuestion(quiz, session.getTargetLanguage());
        if (shownAnswer != null) {
            // 안내문에 정답이 그대로 적혀 있으면 학습자는 베끼기만 하면 된다 (실측 2026-09-22)
            return "the Korean \"question\" already contains the answer \"" + shownAnswer + "\", so the learner only has "
                    + "to copy it. \"question\" quotes the KOREAN meaning of the reply (reply_meaning) and never the "
                    + session.getTargetLanguage() + " expression itself - write reply_meaning in Korean only";
        }
        String filler = OpenAiStoryService.fillerOnlyAnswer(quiz);
        if (filler != null) {
            // "예/아니오" 한 마디를 묻는 퀴즈는 턴만 쓰고 배울 것이 없다 (실측 2026-09-22: 정답이 "はい" 인 퀴즈에 네 턴)
            return "the answer \"" + filler + "\" is just a bare yes/no. The learner spends a whole turn to learn a "
                    + "filler word. Ask what they will DO, CHOOSE, or FEEL so the reply carries real content";
        }
        String invented = OpenAiStoryService.unknownInventedTerm(session, quiz);
        if (invented != null) {
            // 대화에 나온 적 없는 이름을 주관식 정답으로 낸 퀴즈 (실측: AI 가 지어낸 필살기 이름)
            return "the answer \"" + invented + "\" has never been said in this conversation, so the learner cannot "
                    + "possibly type it. Quiz something they decide or already know, or say that name yourself first";
        }
        if (isDuplicateQuizSubject(session.getTestedQuizSubjects(), quiz, session.getTargetLanguage())) {
            // 이미 다뤘던 표현을 형식만 바꿔 다시 낸 퀴즈 (프롬프트 금지 지시를 모델이 어긴 경우)
            return "the answer \"" + quiz.get("correct_answer") + "\" repeats an expression that was already tested in this session";
        }
        if (OpenAiStoryService.isAnswerEchoedInMessage(aiMsg, quiz, session.getTargetLanguage())) {
            // 자기 질문에 들어 있는 단어를 정답으로 낸 앵무새 퀴즈 ("favorite song?" → 'favorite song')
            return "the answer \"" + quiz.get("correct_answer") + "\" appears word-for-word inside your own question, so the learner would just be parroting you";
        }
        if (!OpenAiStoryService.isQuizLinkedToMessage(aiMsg, quiz, session.getTargetLanguage())) {
            // 모델이 적은 "방금 한 질문"이 실제 대사에 없다 → 대사와 무관한 퀴즈 (예시 베끼기 등)
            return "quiz.asked (\"" + quiz.get("asked") + "\") is not the question your ai_message actually ends with, so the quiz does not match what you said";
        }
        if (OpenAiStoryService.isReaskOfRecentUserMessage(session.recentUserMessages(OpenAiStoryService.ALREADY_KNOWN_MESSAGES), quiz)) {
            // 학습자가 방금 한 말(한국어)을 그대로 퀴즈로 되묻는 경우 (실측: "응 주로 미드 해" → "주로 미드 해")
            return "the quiz just re-asks what the learner already told you (reply_meaning \"" + quiz.get("reply_meaning")
                    + "\" repeats their recent message), so it asks for nothing new";
        }
        if (relaxed) {
            return null;
        }
        if (OpenAiStoryService.isDescriptiveReplyMeaning(quiz)) {
            // reply_meaning 이 대사가 아니라 설명이면 퀴즈 질문이 뜻을 잃는다
            return "reply_meaning (\"" + quiz.get("reply_meaning") + "\") is a description, not the learner's spoken line. "
                    + "Write the exact Korean sentence they would say (e.g. '그래, 보스 명령이야'), then quiz that";
        }
        if (OpenAiStoryService.isThirdPersonNarration(quiz)) {
            // 정답이 학습자의 대사가 아니라 제3자 서술인 퀴즈 (실측: "공격 후 상대는?" → '그는 화가 났다')
            return "the answer (\"" + quiz.get("correct_answer") + "\") narrates a third person instead of being the "
                    + "learner's own reply. The answer is always what THEY say, in first or second person";
        }
        if (OpenAiStoryService.isMetaLanguageQuestion(aiMsg, quiz)) {
            // 캐릭터가 선생님이 되어 "뭐라고 말하겠어?"라고 묻는 사례
            return "your question (\"" + quiz.get("asked") + "\") is a meta question about language (asking what they would say / how to say it). "
                    + "Your character does not know there is a quiz - ask a real in-story question instead";
        }
        if (OpenAiStoryService.isQuestionAlreadyAsked(session.getChatHistory(), session.getAskedQuestions(),
                quiz, session.getTargetLanguage())) {
            // 같은 질문을 이전 턴에 이미 했던 경우 (실측: "저녁에 해, 주말에 해?"를 두 번)
            return "you already asked this same question (\"" + quiz.get("asked") + "\") earlier in this conversation";
        }
        return null;
    }

    /**
     * 서버가 확정하지 못한 주관식 답안에 대한 모델 판정을 정규화한다.
     * "correct"/"incorrect" 외의 값(주로 "none")은 "시도 아님"으로 본다.
     */
    private static String normalizeModelVerdict(Object value) {
        String result = textOrDefault(value, "none").toLowerCase();
        return ("correct".equals(result) || "incorrect".equals(result)) ? result : OpenAiStoryService.NOT_ATTEMPT;
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
