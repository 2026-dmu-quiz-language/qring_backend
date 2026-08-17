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

    // 실시간 세션 메모리 스토어 (sessionId -> StorySession)
    private final ConcurrentHashMap<String, StorySession> sessionStore = new ConcurrentHashMap<>();

    /**
     * 1단계: 스토리 세션 시작 및 포인트 차감 (-30pt)
     */
    @Transactional
    public StoryStartResponse startStorySession(Long userId, StoryStartRequest request) {
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

        // 3. 세션 객체 생성
        String sessionId = "sess-" + UUID.randomUUID().toString().substring(0, 8);
        StorySession session = StorySession.builder()
                .sessionId(sessionId)
                .userId(userId)
                .characterName(request.getCharacterName())
                .situationDescription(request.getSituationDescription())
                .tone(request.getTone())
                .targetLanguage(request.getTargetLanguage() != null ? request.getTargetLanguage() : "English")
                .levelCode(user.getLevelCode() != null ? user.getLevelCode() : 1)
                .build();

        // 4. OpenAI 첫 오프닝 생성
        Map<String, Object> openingData = openAiStoryService.generateOpening(request, user.getLevelCode());
        String aiFirstMsg = (String) openingData.getOrDefault("ai_message", "Hello! Nice to meet you.");
        String aiFirstTrans = (String) openingData.getOrDefault("translation", "안녕! 만나서 반가워.");

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

        // 1. 사용자 메시지 기록
        session.addMessage("user", request.getUserMessage());
        session.incrementTurnsSinceLastQuiz();

        // 2. OpenAI 턴 응답 생성 (사용자 대답 반영 + 실시간 피드백/퀴즈 여부)
        Map<String, Object> turnResponse = openAiStoryService.generateTurnResponse(session, request.getUserMessage());

        String aiMsg = (String) turnResponse.getOrDefault("ai_message", "Got it!");
        String translation = (String) turnResponse.getOrDefault("translation", "알겠어!");
        Boolean isQuiz = (Boolean) turnResponse.getOrDefault("is_quiz", false);
        Boolean isCompleted = (Boolean) turnResponse.getOrDefault("is_completed", false);

        @SuppressWarnings("unchecked")
        Map<String, Object> quiz = isQuiz ? (Map<String, Object>) turnResponse.get("quiz") : null;

        if (Boolean.TRUE.equals(isQuiz)) {
            session.incrementQuizCount();
            if (quiz != null) {
                String subject = (String) quiz.get("correct_answer");
                if (subject == null) {
                    subject = (String) quiz.get("question");
                }
                session.addTestedQuizSubject(subject);
            }
        }

        // 3. AI 대사 히스토리에 추가
        session.addMessage("assistant", aiMsg);

        if (Boolean.TRUE.equals(isCompleted)) {
            session.setCompleted(true);
            log.info("[InteractiveStory] 세션 {} 완결 처리 (총 퀴즈 5개 완료)", sessionId);
        }

        return StoryChatResponse.builder()
                .sessionId(sessionId)
                .aiMessage(aiMsg)
                .translation(translation)
                .isQuiz(isQuiz)
                .quiz(quiz)
                .currentQuizCount(session.getQuizCount())
                .isCompleted(session.isCompleted())
                .build();
    }
}
