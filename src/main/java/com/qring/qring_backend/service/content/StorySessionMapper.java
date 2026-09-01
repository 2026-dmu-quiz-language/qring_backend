package com.qring.qring_backend.service.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qring.qring_backend.domain.content.StorySession;
import com.qring.qring_backend.domain.content.StorySessionEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 메모리 세션(StorySession) ↔ DB 레코드(StorySessionEntity) 변환.
 * 서버 재시작 후에도 진행 중 세션을 복원할 수 있도록 진행 상태 전부를 JSON 으로 직렬화한다.
 */
@Component
public class StorySessionMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 세션 시작 시 새 DB 레코드 생성. */
    public StorySessionEntity toNewEntity(StorySession session) {
        StorySessionEntity entity = new StorySessionEntity();
        entity.setSessionId(session.getSessionId());
        entity.setUserId(session.getUserId());
        entity.setCharacterName(session.getCharacterName());
        entity.setSituationDescription(session.getSituationDescription());
        entity.setTone(session.getTone());
        entity.setTargetLanguage(session.getTargetLanguage());
        entity.setLevelCode(session.getLevelCode());
        entity.setStatus(StorySessionEntity.STATUS_IN_PROGRESS);
        entity.setCreatedAt(session.getCreatedAt() != null ? session.getCreatedAt() : LocalDateTime.now());
        applyState(entity, session);
        return entity;
    }

    /** 매 턴 이후 진행 상태를 기존 레코드에 반영. */
    public void applyState(StorySessionEntity entity, StorySession session) {
        entity.setQuizCount(session.getQuizCount());
        entity.setIsCompleted(session.isCompleted());
        entity.setChatHistory(writeJson(session.getChatHistory()));
        entity.setTimeline(writeJson(session.getTimeline()));

        Map<String, Object> runtime = new HashMap<>();
        runtime.put("turnsSinceLastQuiz", session.getTurnsSinceLastQuiz());
        runtime.put("pendingQuiz", session.getPendingQuiz());
        runtime.put("testedQuizSubjects", session.getTestedQuizSubjects());
        runtime.put("usedQuizTypes", session.getUsedQuizTypes());
        entity.setRuntimeState(writeJson(runtime));

        entity.setUpdatedAt(LocalDateTime.now());
    }

    /** DB 레코드에서 메모리 세션 복원 (서버 재시작 후 이어하기). */
    public StorySession toDomain(StorySessionEntity entity) {
        Map<String, Object> runtime = readJson(entity.getRuntimeState(), new TypeReference<>() {});

        StorySession session = StorySession.builder()
                .sessionId(entity.getSessionId())
                .userId(entity.getUserId())
                .characterName(entity.getCharacterName())
                .situationDescription(entity.getSituationDescription())
                .tone(entity.getTone())
                .targetLanguage(entity.getTargetLanguage())
                .levelCode(entity.getLevelCode() != null ? entity.getLevelCode() : 1)
                .quizCount(entity.getQuizCount() != null ? entity.getQuizCount() : 0)
                .isCompleted(Boolean.TRUE.equals(entity.getIsCompleted()))
                .chatHistory(readJson(entity.getChatHistory(), new TypeReference<List<Map<String, String>>>() {}))
                .timeline(readJson(entity.getTimeline(), new TypeReference<List<Map<String, Object>>>() {}))
                .createdAt(entity.getCreatedAt())
                .build();

        Object turns = runtime.get("turnsSinceLastQuiz");
        session.setTurnsSinceLastQuiz(turns instanceof Number n ? n.intValue() : 0);
        session.setPendingQuiz(castMap(runtime.get("pendingQuiz")));
        session.setTestedQuizSubjects(castStringList(runtime.get("testedQuizSubjects")));
        session.setUsedQuizTypes(castStringList(runtime.get("usedQuizTypes")));
        return session;
    }

    public List<Map<String, Object>> parseTimeline(String json) {
        return readJson(json, new TypeReference<>() {});
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("세션 상태 직렬화 실패: " + e.getMessage(), e);
        }
    }

    private <T> T readJson(String json, TypeReference<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalStateException("세션 상태 역직렬화 실패: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : null;
    }

    private static List<String> castStringList(Object value) {
        List<String> result = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object o : list) {
                result.add(String.valueOf(o));
            }
        }
        return result;
    }
}
