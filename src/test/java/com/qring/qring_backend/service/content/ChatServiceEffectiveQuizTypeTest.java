package com.qring.qring_backend.service.content;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 스토리 퀴즈 유형 보정: 언어 공통 quiz_type 이 그 언어 본문과 어긋나도 프론트가 그릴 수 있는 유형으로 맞춘다. */
class ChatServiceEffectiveQuizTypeTest {

    @Test
    @DisplayName("options 가 없으면 quiz_type 이 무엇이든 subjective (zh 가 주관식인데 en 기준 contextual 로 저장된 슬롯)")
    void noOptions_isSubjective() {
        assertEquals("subjective", ChatService.effectiveQuizType("contextual", null));
        assertEquals("subjective", ChatService.effectiveQuizType("multiple_choice", "[]"));
        assertEquals("subjective", ChatService.effectiveQuizType("fill_in_blank", " "));
        assertEquals("subjective", ChatService.effectiveQuizType("subjective", null));
    }

    @Test
    @DisplayName("options 가 있는데 quiz_type 이 subjective 면 multiple_choice (zh 가 선택지형인데 en 기준 subjective 로 저장된 슬롯)")
    void optionsButSubjective_isMultipleChoice() {
        assertEquals("multiple_choice", ChatService.effectiveQuizType("subjective", "[\"a\",\"b\",\"c\",\"d\"]"));
        assertEquals("multiple_choice", ChatService.effectiveQuizType(null, "[\"a\",\"b\"]"));
    }

    @Test
    @DisplayName("정상 조합은 quiz_type 그대로 (fill_in_blank·contextual 라벨 유지)")
    void consistent_unchanged() {
        assertEquals("multiple_choice", ChatService.effectiveQuizType("multiple_choice", "[\"a\",\"b\"]"));
        assertEquals("fill_in_blank", ChatService.effectiveQuizType("fill_in_blank", "[\"a\",\"b\"]"));
        assertEquals("contextual", ChatService.effectiveQuizType("contextual", "[\"a\",\"b\"]"));
    }
}
