package com.qring.qring_backend.service.competition;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qring.qring_backend.domain.competition.CompetitionQuizContent;
import com.qring.qring_backend.domain.competition.CompetitionQuizContentRepository;
import com.qring.qring_backend.domain.competition.CompetitionQuizDetail;
import com.qring.qring_backend.domain.competition.CompetitionQuizDetailRepository;
import com.qring.qring_backend.dto.competition.CompetitionQuizImportDto;
import com.qring.qring_backend.dto.competition.CompetitionQuizImportDto.LevelDto;
import com.qring.qring_backend.dto.competition.CompetitionQuizImportDto.QuestionDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompetitionQuizImportService {

    private final CompetitionQuizDetailRepository quizDetailRepository;
    private final CompetitionQuizContentRepository quizContentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * json 하나(언어 1개, 레벨 여러 개)를 통째로 받아 저장.
     * (level, originId) 조합으로 quiz_detail 존재 여부를 확인해서
     * - 없으면 신규 생성 (같은 문제의 첫 언어)
     * - 있으면 기존 quiz_id 재사용 (다른 언어 추가)
     * quiz_content는 (quiz_id, lang_code) 유니크 제약이 있으므로 언어별 1회만 저장됨.
     */
    @Transactional
    public ImportResult importQuizSet(CompetitionQuizImportDto dto) {
        String langCode = dto.getLanguage();
        int savedDetail = 0;
        int savedContent = 0;

        for (LevelDto levelDto : dto.getLevels()) {
            for (QuestionDto q : levelDto.getQuestions()) {

                CompetitionQuizDetail quizDetail = quizDetailRepository
                        .findByLevelAndOriginId(levelDto.getLevel(), q.getId())
                        .orElseGet(() -> {
                            CompetitionQuizDetail newDetail = new CompetitionQuizDetail();
                            newDetail.setLevel(levelDto.getLevel());
                            newDetail.setOriginId(q.getId());
                            newDetail.setQuizType(q.getType());
                            return newDetail;
                        });

                if (quizDetail.getQuizId() == null) {
                    quizDetailRepository.save(quizDetail);
                    savedDetail++;
                }

                // 이미 같은 언어로 저장된 문제면 스킵 (재실행 대비)
                boolean alreadyExists = quizContentRepository
                        .findByQuizIdAndLangCode(quizDetail.getQuizId(), langCode)
                        .isPresent();
                if (alreadyExists) {
                    continue;
                }

                CompetitionQuizContent content = new CompetitionQuizContent();
                content.setQuizDetail(quizDetail);
                content.setLangCode(langCode);
                content.setQuestion(q.getQuestion());
                content.setKorean(q.getKorean());
                content.setAnswer(q.getAnswer());
                content.setTiles(toJson(q.getTiles()));
                content.setAnswerTiles(toJson(q.getAnswerTiles()));
                content.setDistractorTiles(toJson(q.getDistractorTiles()));
                content.setOptions(toJson(q.getOptions()));
                content.setAcceptableAnswers(toJson(q.getAcceptableAnswers()));

                quizContentRepository.save(content);
                savedContent++;
            }
        }

        log.info("컴피티션 문제 import 완료 - lang: {}, quizDetail 신규: {}, quizContent 신규: {}",
                langCode, savedDetail, savedContent);

        return new ImportResult(savedDetail, savedContent);
    }

    private String toJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON 변환 실패: " + list, e);
        }
    }

    public record ImportResult(int savedDetailCount, int savedContentCount) {}
}