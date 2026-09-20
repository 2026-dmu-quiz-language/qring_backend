package com.qring.qring_backend.service.competition;

import com.qring.qring_backend.domain.competition.CompetitionQuizContent;
import com.qring.qring_backend.domain.competition.CompetitionQuizContentRepository;
import com.qring.qring_backend.domain.competition.CompetitionQuizDetail;
import com.qring.qring_backend.domain.competition.CompetitionQuizDetailRepository;
import com.qring.qring_backend.dto.competition.CompetitionQuizImportDto;
import com.qring.qring_backend.dto.competition.CompetitionQuizImportDto.LevelDto;
import com.qring.qring_backend.dto.competition.CompetitionQuizImportDto.QuestionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * import: 세트(quizSet)·레벨·id 로 detail 을 찾고, 본문 모양이 유형과 안 맞으면 파일 전체 거절.
 * 언어 간 유형 차이는 경고만 (언어별 파일은 같은 id 가 같은 문제가 아님).
 */
class CompetitionQuizImportServiceTest {

    private CompetitionQuizDetailRepository detailRepository;
    private CompetitionQuizContentRepository contentRepository;
    private CompetitionQuizImportService service;

    @BeforeEach
    void setUp() {
        detailRepository = mock(CompetitionQuizDetailRepository.class);
        contentRepository = mock(CompetitionQuizContentRepository.class);
        service = new CompetitionQuizImportService(detailRepository, contentRepository);
    }

    @Test
    @DisplayName("기존(en) detail 이 multiple_choice 인데 ja 파일이 word_arrange 여도 저장한다 (경고만)")
    void typeMismatchAcrossLanguages_stillSaved() {
        CompetitionQuizDetail existing = detail(7L, "02", 1, 12, "multiple_choice");
        when(detailRepository.findBySetKeyAndLevelAndOriginId("02", 1, 12)).thenReturn(Optional.of(existing));
        when(contentRepository.findByQuizIdAndLangCode(anyLong(), anyString())).thenReturn(Optional.empty());

        CompetitionQuizImportDto dto = file("ja", 1,
                question(12, "word_arrange", null, List.of("私は", "学生", "です")));

        CompetitionQuizImportService.ImportResult result = service.importQuizSet(dto, "02");

        assertEquals(0, result.savedDetailCount());   // 기존 detail 재사용, 유형은 en 것 그대로
        assertEquals(1, result.savedContentCount());
        ArgumentCaptor<CompetitionQuizContent> saved = ArgumentCaptor.forClass(CompetitionQuizContent.class);
        verify(contentRepository).save(saved.capture());
        assertEquals("[\"私は\",\"学生\",\"です\"]", saved.getValue().getTiles());   // 본문(tiles)은 그대로 → 출제 시 word_arrange
    }

    @Test
    @DisplayName("본문 모양이 유형과 안 맞으면(객관식인데 options 없음 등) 파일 전체 거절, 저장 없음")
    void shapeMismatch_rejected() {
        when(detailRepository.findBySetKeyAndLevelAndOriginId(anyString(), anyInt(), anyInt())).thenReturn(Optional.empty());

        CompetitionQuizImportDto dto = file("zh", 2,
                question(3, "multiple_choice", null, null),
                question(4, "word_arrange", null, null),
                question(5, "subjective", List.of("a", "b"), null));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> service.importQuizSet(dto, "01"));

        assertTrue(e.getMessage().contains("3건"), e.getMessage());
        assertTrue(e.getMessage().contains("level=2 id=3"), e.getMessage());
        assertTrue(e.getMessage().contains("level=2 id=4"), e.getMessage());
        assertTrue(e.getMessage().contains("level=2 id=5"), e.getMessage());
        verify(detailRepository, never()).save(any());
        verify(contentRepository, never()).save(any());
    }

    @Test
    @DisplayName("같은 (level, id) 라도 세트가 다르면 새 detail 을 만든다 (_01 과 _02 가 서로 덮어쓰지 않음)")
    void differentSet_createsNewDetail() {
        when(detailRepository.findBySetKeyAndLevelAndOriginId(eq("01"), anyInt(), anyInt())).thenReturn(Optional.empty());
        when(contentRepository.findByQuizIdAndLangCode(any(), anyString())).thenReturn(Optional.empty());

        CompetitionQuizImportDto dto = file("en", 1,
                question(1, "multiple_choice", List.of("a", "b"), null));

        CompetitionQuizImportService.ImportResult result = service.importQuizSet(dto, "01");

        assertEquals(1, result.savedDetailCount());
        assertEquals(1, result.savedContentCount());
        ArgumentCaptor<CompetitionQuizDetail> saved = ArgumentCaptor.forClass(CompetitionQuizDetail.class);
        verify(detailRepository).save(saved.capture());
        assertEquals("01", saved.getValue().getSetKey());
        assertEquals(1, saved.getValue().getOriginId());
        assertEquals("multiple_choice", saved.getValue().getQuizType());
    }

    @Test
    @DisplayName("유형·모양이 맞으면 같은 세트의 기존 detail 을 재사용해 새 언어 본문을 저장한다")
    void consistent_savesContentOnExistingDetail() {
        CompetitionQuizDetail existing = detail(7L, "02", 1, 12, "multiple_choice");
        when(detailRepository.findBySetKeyAndLevelAndOriginId("02", 1, 12)).thenReturn(Optional.of(existing));
        when(contentRepository.findByQuizIdAndLangCode(anyLong(), anyString())).thenReturn(Optional.empty());

        CompetitionQuizImportDto dto = file("ja", 1,
                question(12, "multiple_choice", List.of("はい", "いいえ"), null));

        CompetitionQuizImportService.ImportResult result = service.importQuizSet(dto, "02");

        assertEquals(0, result.savedDetailCount());
        assertEquals(1, result.savedContentCount());
        ArgumentCaptor<CompetitionQuizContent> saved = ArgumentCaptor.forClass(CompetitionQuizContent.class);
        verify(contentRepository).save(saved.capture());
        assertEquals("ja", saved.getValue().getLangCode());
        assertEquals(existing, saved.getValue().getQuizDetail());
        assertEquals("[\"はい\",\"いいえ\"]", saved.getValue().getOptions());
    }

    @Test
    @DisplayName("quizSet 이 비었거나 형식이 틀리면 400")
    void invalidSetKey_rejected() {
        CompetitionQuizImportDto dto = file("en", 1, question(1, "subjective", null, null));
        assertThrows(IllegalArgumentException.class, () -> service.importQuizSet(dto, ""));
        assertThrows(IllegalArgumentException.class, () -> service.importQuizSet(dto, "세트 1"));
        verify(detailRepository, never()).save(any());
    }

    /* ---------- fixtures ---------- */

    private static CompetitionQuizDetail detail(Long id, String setKey, int level, int originId, String type) {
        CompetitionQuizDetail d = new CompetitionQuizDetail();
        d.setQuizId(id);
        d.setSetKey(setKey);
        d.setLevel(level);
        d.setOriginId(originId);
        d.setQuizType(type);
        return d;
    }

    private static QuestionDto question(int id, String type, List<String> options, List<String> tiles) {
        QuestionDto q = new QuestionDto();
        q.setId(id);
        q.setType(type);
        q.setQuestion("Q" + id);
        q.setAnswer("A" + id);
        q.setOptions(options);
        q.setTiles(tiles);
        return q;
    }

    private static CompetitionQuizImportDto file(String lang, int level, QuestionDto... questions) {
        LevelDto levelDto = new LevelDto();
        levelDto.setLevel(level);
        levelDto.setQuestions(List.of(questions));
        CompetitionQuizImportDto dto = new CompetitionQuizImportDto();
        dto.setLanguage(lang);
        dto.setLevels(List.of(levelDto));
        return dto;
    }
}
