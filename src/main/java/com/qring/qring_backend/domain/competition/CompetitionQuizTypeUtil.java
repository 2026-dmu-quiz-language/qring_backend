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

    /** JSON 배열 문자열에 원소가 있는지 (null, 빈 문자열, "[]", "null" 은 없음). */
    public static boolean hasJsonItems(String json) {
        if (json == null) {
            return false;
        }
        String trimmed = json.trim();
        return !trimmed.isEmpty() && !trimmed.equals("[]") && !trimmed.equalsIgnoreCase("null");
    }
}