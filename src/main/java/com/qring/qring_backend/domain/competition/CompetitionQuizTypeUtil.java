package com.qring.qring_backend.domain.competition;

/**
 * 컴피티션 문제의 실제 유형 판정 유틸.
 * detail.quiz_type 이 아니라 본문(quiz_content)에 무엇이 들어 있는지로 판정한다:
 * tiles 가 있으면 word_arrange, options 가 있으면 multiple_choice, 둘 다 없으면 subjective.
 * quiz_type 은 (level, origin_id) 단위로 첫 import 언어의 값이 고정되는 구조라 다른 언어 본문과 어긋날 수 있어서,
 * 본문 기준으로 판정해야 항상 프론트가 그릴 수 있는 모양이 나온다.
 * CompetitionMatchService(출제)와 IncorrectService(오답 재풀이) 양쪽에서 공용으로 사용.
 */
public final class CompetitionQuizTypeUtil {

    private CompetitionQuizTypeUtil() {
    }

    public static String effectiveType(CompetitionQuizContent qc) {
        if (hasJsonItems(qc.getTiles())) {
            return "word_arrange";
        }
        if (hasJsonItems(qc.getOptions())) {
            return "multiple_choice";
        }
        return "subjective";
    }

    /**
     * 스토리 원본 문제(quiz_content)의 실제 유형 판정.
     * 컴피티션 본문과 같은 이유로 detail.quiz_type 을 그대로 믿지 않는다 —
     * options 가 비어 있는데 quiz_type 이 multiple_choice 인 row 가 있어서,
     * 그대로 내보내면 프론트가 보기 없는 객관식을 그린다.
     * fill_in_blank 는 컴피티션/오답 재풀이에서 주관식으로 낸다.
     * CompetitionMatchService(컴피티션 출제)와 IncorrectService(오답 재풀이, STORY·COMPETITION 양쪽) 공용.
     */
    public static String effectiveStoryType(String rawType, String optionsJson) {
        if (!hasJsonItems(optionsJson)) {
            return "subjective";
        }
        if ("fill_in_blank".equals(rawType)) {
            return "subjective";
        }
        if (rawType == null || "subjective".equals(rawType)) {
            return "multiple_choice";
        }
        return rawType;
    }

    /** JSON 배열 문자열에 원소가 있는지 (null, 빈 문자열, "[]", "null" 은 없음). */
    public static boolean hasJsonItems(String json) {
        if (json == null) {
            return false;
        }
        String trimmed = json.trim();
        return !trimmed.isEmpty() && !trimmed.equals("[]") && !trimmed.equalsIgnoreCase("null");
    }
}