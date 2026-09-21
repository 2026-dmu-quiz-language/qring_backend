package com.qring.qring_backend.domain.user;

import java.util.Locale;
import java.util.Optional;

/**
 * 학습 언어. {@code users.language} 에 저장되는 코드(EN/JA/ZH)와, 프롬프트에 넣는 영어 이름,
 * 화면용 한글 이름, 띄어쓰기 여부를 한곳에 둔다.
 *
 * 2026-09-22: 인터랙티브 스토리가 요청의 targetLanguage 대신 토큰 사용자의 설정 언어를 쓰도록 바뀌면서 추가.
 * 일본어·중국어는 단어 사이에 공백이 없어, 단어 단위로 자르던 서버 규칙(타일·채점·겹침 검사)이 문자 단위로 분기한다.
 */
public enum LearningLanguage {

    EN("EN", "English", "영어", true),
    JA("JA", "Japanese", "일본어", false),
    /** 간체 고정 (팀 결정 2026-09-22). 프롬프트 이름에 표기해 번체가 섞이지 않게 한다. */
    ZH("ZH", "Chinese (Simplified)", "중국어", false);

    /** 사용자 언어가 비어 있거나(온보딩 전) 모르는 코드일 때 쓰는 기본값. */
    public static final LearningLanguage DEFAULT = EN;

    private final String code;
    private final String promptName;
    private final String koreanName;
    private final boolean spaced;

    LearningLanguage(String code, String promptName, String koreanName, boolean spaced) {
        this.code = code;
        this.promptName = promptName;
        this.koreanName = koreanName;
        this.spaced = spaced;
    }

    public String getCode() {
        return code;
    }

    /** 프롬프트의 "Target Language: ..." 와 세션의 targetLanguage 에 들어가는 이름. */
    public String getPromptName() {
        return promptName;
    }

    public String getKoreanName() {
        return koreanName;
    }

    /** 단어 사이에 공백을 쓰는 언어인지 (영어 true, 일본어·중국어 false). */
    public boolean usesSpaces() {
        return spaced;
    }

    /** users.language 코드(대소문자·공백 무시)로 찾는다. 없거나 모르는 코드면 empty. */
    public static Optional<LearningLanguage> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        for (LearningLanguage language : values()) {
            if (language.code.equals(normalized)) {
                return Optional.of(language);
            }
        }
        return Optional.empty();
    }

    /** 코드 → 프롬프트 이름. 모르는 코드는 기본값(English). */
    public static String promptNameFor(String code) {
        return fromCode(code).orElse(DEFAULT).promptName;
    }

    /**
     * 세션에 저장된 프롬프트 이름("Japanese", "Chinese (Simplified)", 옛 세션의 "English")으로 찾는다.
     * 이름의 첫 단어만 비교하므로 "Chinese" 와 "Chinese (Simplified)" 는 같은 언어다.
     */
    public static Optional<LearningLanguage> fromPromptName(String promptName) {
        if (promptName == null || promptName.isBlank()) {
            return Optional.empty();
        }
        String head = promptName.trim().toLowerCase(Locale.ROOT).split("[\\s(]+")[0];
        for (LearningLanguage language : values()) {
            String languageHead = language.promptName.toLowerCase(Locale.ROOT).split("[\\s(]+")[0];
            if (languageHead.equals(head)) {
                return Optional.of(language);
            }
        }
        return Optional.empty();
    }

    /** 세션의 targetLanguage 가 띄어쓰기를 쓰는 언어인지. 모르는 언어(한국어 등)는 띄어쓰기를 쓰는 것으로 본다. */
    public static boolean usesSpaces(String targetLanguage) {
        return fromPromptName(targetLanguage).map(LearningLanguage::usesSpaces).orElse(true);
    }
}
