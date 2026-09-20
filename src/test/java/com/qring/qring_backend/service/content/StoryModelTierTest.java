package com.qring.qring_backend.service.content;

import com.qring.qring_backend.domain.content.StorySession;
import com.qring.qring_backend.domain.content.StorySessionEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 모델 티어: 정규화, 티어별 모델·비용, 세션 영속화, 모델 선택. */
class StoryModelTierTest {

    @Test
    @DisplayName("modelTier 요청값 정규화: 생략은 기본, 대소문자 무시, 그 외는 400 으로 이어지는 예외")
    void normalizesRequestedTier() {
        assertEquals("standard", StoryModelTier.normalize(null));
        assertEquals("standard", StoryModelTier.normalize("  "));
        assertEquals("standard", StoryModelTier.normalize("standard"));
        assertEquals("premium", StoryModelTier.normalize("Premium"));
        assertThrows(IllegalArgumentException.class, () -> StoryModelTier.normalize("ultra"));
    }

    @Test
    @DisplayName("기본값: 기본 모델 400pt/gpt-4.1-mini, 프리미엄 550pt/gpt-4.1, 이어하기는 양쪽 100pt")
    void defaultModelsAndCosts() {
        StoryModelTier tiers = new StoryModelTier();
        assertEquals("gpt-4.1-mini", tiers.modelFor("standard"));
        assertEquals("gpt-4.1", tiers.modelFor("premium"));
        assertEquals(400, tiers.startCostFor("standard"));
        assertEquals(550, tiers.startCostFor("premium"));
        assertEquals(150, tiers.upgradeCost());
        assertEquals(100, tiers.extendCostFor("standard"));
        assertEquals(100, tiers.extendCostFor("premium"));
        assertEquals("기본 모델", StoryModelTier.labelOf("standard"));
        assertEquals("프리미엄 모델", StoryModelTier.labelOf("premium"));
    }

    @Test
    @DisplayName("세션 티어에 따라 호출 모델이 정해지고, 스모크 테스트 강제도 동작한다")
    void resolvesModelBySessionTier() {
        OpenAiStoryService service = new OpenAiStoryService();
        StorySession standard = StorySession.builder().sessionId("a").modelTier("standard").build();
        StorySession premium = StorySession.builder().sessionId("b").modelTier("premium").build();
        assertEquals("gpt-4.1-mini", service.resolveModel(standard));
        assertEquals("gpt-4.1", service.resolveModel(premium));
        assertEquals("gpt-4.1-mini", service.resolveModel(null));

        service.overrideModelForTests("gpt-test");
        assertEquals("gpt-test", service.resolveModel(premium));
    }

    @Test
    @DisplayName("티어는 runtime_state 로 저장·복원되고, 티어 도입 전 세션은 기본 모델로 복원된다")
    void tierSurvivesPersistence() {
        StorySessionMapper mapper = new StorySessionMapper();
        StorySession session = StorySession.builder()
                .sessionId("sess-1").userId(1L).characterName("제임스").situationDescription("교도소 면회")
                .tone("까칠하게").targetLanguage("English").levelCode(1).modelTier("premium").build();
        session.addAssistantMessage("Hey.", "야.");

        StorySessionEntity entity = mapper.toNewEntity(session);
        assertTrue(entity.getRuntimeState().contains("\"modelTier\":\"premium\""));
        assertEquals("premium", mapper.readModelTier(entity));
        assertEquals("premium", mapper.toDomain(entity).getModelTier());

        assertEquals("standard", StorySessionMapper.readModelTier(Map.of("quizLimit", 5)));
        assertEquals("standard", StorySessionMapper.readModelTier((Map<String, Object>) null));
    }

    @Test
    @DisplayName("이야기 메모와 퀴즈 턴 실패 횟수도 runtime_state 로 저장·복원된다")
    void storyNotesSurvivePersistence() {
        StorySessionMapper mapper = new StorySessionMapper();
        StorySession session = StorySession.builder()
                .sessionId("sess-2").userId(1L).characterName("피카츄").situationDescription("시험장")
                .tone("다정하게").targetLanguage("English").levelCode(1).build();
        session.setStorySoFar("같이 점심 먹기로 함");
        session.setFailedQuizTurns(2);
        session.addAssistantMessage("Hey.", "야.");

        StorySession restored = mapper.toDomain(mapper.toNewEntity(session));
        assertEquals("같이 점심 먹기로 함", restored.getStorySoFar());
        assertEquals(2, restored.getFailedQuizTurns());

        restored.recordQuiz(Map.of("quiz_type", "subjective", "correct_answer", "karaoke"));
        assertEquals(0, restored.getFailedQuizTurns(), "퀴즈가 수락되면 실패 횟수는 0");
    }

    @Test
    @DisplayName("이어하기 표시에 차감 포인트가 남는다")
    void extensionMarkerRecordsCharge() {
        StorySession session = StorySession.builder().sessionId("s").build();
        session.extendQuizLimit(5);
        session.addExtensionMarker(100);
        Map<String, Object> event = session.getTimeline().get(0);
        assertEquals("extension", event.get("type"));
        assertEquals(10, event.get("quiz_limit"));
        assertEquals(100, event.get("charged_points"));
    }
}
