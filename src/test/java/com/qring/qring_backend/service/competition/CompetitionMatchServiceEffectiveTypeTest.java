package com.qring.qring_backend.service.competition;

import com.qring.qring_backend.domain.competition.CompetitionQuizContent;
import com.qring.qring_backend.domain.competition.CompetitionQuizDetail;
import com.qring.qring_backend.domain.competition.CompetitionQuizTypeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 출제 유형은 detail.quiz_type 이 아니라 본문 모양으로 정한다.
 * ja/zh 본문이 en 기준 quiz_type 에 묶여 "객관식인데 선택지 없음 / 배열인데 타일 없음" 으로 나가던 문제의 회귀 방지.
 * 판정 로직은 CompetitionQuizTypeUtil 로 분리됨 (IncorrectService 재풀이 조회와 공용).
 */
class CompetitionMatchServiceEffectiveTypeTest {

    @Test
    @DisplayName("tiles 가 있으면 detail 이 multiple_choice 라고 해도 word_arrange")
    void tilesWinOverDetailType() {
        CompetitionQuizContent qc = content("multiple_choice", null, "[\"私は\",\"学生\",\"です\"]");
        assertEquals("word_arrange", CompetitionQuizTypeUtil.effectiveType(qc));
    }

    @Test
    @DisplayName("options 가 있으면 detail 이 word_arrange 라고 해도 multiple_choice")
    void optionsWinOverDetailType() {
        CompetitionQuizContent qc = content("word_arrange", "[\"はい\",\"いいえ\"]", null);
        assertEquals("multiple_choice", CompetitionQuizTypeUtil.effectiveType(qc));
    }

    @Test
    @DisplayName("둘 다 없으면 subjective (빈 배열·null 문자열도 없음으로 본다)")
    void neither_isSubjective() {
        assertEquals("subjective", CompetitionQuizTypeUtil.effectiveType(content("multiple_choice", null, null)));
        assertEquals("subjective", CompetitionQuizTypeUtil.effectiveType(content("word_arrange", "[]", " [] ")));
        assertEquals("subjective", CompetitionQuizTypeUtil.effectiveType(content("subjective", "null", null)));
    }

    @Test
    @DisplayName("정상 데이터(en)는 detail 유형과 본문 유형이 일치한다")
    void consistentData_unchanged() {
        assertEquals("multiple_choice", CompetitionQuizTypeUtil.effectiveType(content("multiple_choice", "[\"a\",\"b\"]", null)));
        assertEquals("word_arrange", CompetitionQuizTypeUtil.effectiveType(content("word_arrange", null, "[\"I\",\"am\"]")));
        assertEquals("subjective", CompetitionQuizTypeUtil.effectiveType(content("subjective", null, null)));
    }

    @Test
    void hasJsonItems() {
        assertTrue(CompetitionQuizTypeUtil.hasJsonItems("[\"x\"]"));
        assertFalse(CompetitionQuizTypeUtil.hasJsonItems(null));
        assertFalse(CompetitionQuizTypeUtil.hasJsonItems(""));
        assertFalse(CompetitionQuizTypeUtil.hasJsonItems("[]"));
        assertFalse(CompetitionQuizTypeUtil.hasJsonItems("NULL"));
    }

    private static CompetitionQuizContent content(String detailType, String options, String tiles) {
        CompetitionQuizDetail detail = new CompetitionQuizDetail();
        detail.setQuizType(detailType);
        CompetitionQuizContent qc = new CompetitionQuizContent();
        qc.setQuizDetail(detail);
        qc.setOptions(options);
        qc.setTiles(tiles);
        return qc;
    }
}