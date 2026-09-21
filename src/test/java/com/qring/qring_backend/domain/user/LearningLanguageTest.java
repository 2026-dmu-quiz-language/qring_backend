package com.qring.qring_backend.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** users.language 코드 ↔ 프롬프트 언어명 매핑과 띄어쓰기 판별. */
class LearningLanguageTest {

    @Test
    @DisplayName("코드는 대소문자·공백을 무시하고 찾고, 모르는 코드나 빈 값은 empty")
    void findsByCode() {
        assertEquals(Optional.of(LearningLanguage.JA), LearningLanguage.fromCode("ja"));
        assertEquals(Optional.of(LearningLanguage.EN), LearningLanguage.fromCode(" EN "));
        assertEquals(Optional.of(LearningLanguage.ZH), LearningLanguage.fromCode("ZH"));
        assertTrue(LearningLanguage.fromCode("KO").isEmpty());
        assertTrue(LearningLanguage.fromCode(null).isEmpty());
        assertTrue(LearningLanguage.fromCode("  ").isEmpty());
    }

    @Test
    @DisplayName("프롬프트 이름: EN→English, JA→Japanese, ZH→Chinese (Simplified), 그 외는 English")
    void promptNames() {
        assertEquals("English", LearningLanguage.promptNameFor("EN"));
        assertEquals("Japanese", LearningLanguage.promptNameFor("JA"));
        assertEquals("Chinese (Simplified)", LearningLanguage.promptNameFor("zh"));
        assertEquals("English", LearningLanguage.promptNameFor(null), "온보딩 전 사용자는 기본값");
        assertEquals("English", LearningLanguage.promptNameFor("xx"));
    }

    @Test
    @DisplayName("세션에 저장된 이름으로 띄어쓰기 여부를 판별한다 (모르는 언어는 띄어쓰기 있는 것으로)")
    void usesSpacesByPromptName() {
        assertTrue(LearningLanguage.usesSpaces("English"));
        assertFalse(LearningLanguage.usesSpaces("Japanese"));
        assertFalse(LearningLanguage.usesSpaces("Chinese (Simplified)"));
        assertFalse(LearningLanguage.usesSpaces("chinese"), "이름의 첫 단어만 비교한다");
        assertTrue(LearningLanguage.usesSpaces("Korean"));
        assertTrue(LearningLanguage.usesSpaces(null));
    }
}
