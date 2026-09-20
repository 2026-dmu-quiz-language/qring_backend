package com.qring.qring_backend.service.competition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    /** setKey 허용 형식 — 파일명 접미사 수준의 짧은 식별자만 (예: 01, 02, default). */
    private static final java.util.regex.Pattern SET_KEY = java.util.regex.Pattern.compile("[A-Za-z0-9_-]{1,30}");

    /**
     * json 하나(언어 1개, 레벨 여러 개)를 통째로 받아 저장.
     * (setKey, level, originId) 조합으로 quiz_detail 존재 여부를 확인해서
     * - 없으면 신규 생성 (그 세트·id 의 첫 언어)
     * - 있으면 기존 quiz_id 재사용 (다른 언어 추가)
     * quiz_content는 (quiz_id, lang_code) 유니크 제약이 있으므로 언어별 1회만 저장됨.
     *
     * setKey 는 세트 파일(_01, _02 …)을 구분하는 값. 세트마다 id 가 1 부터 다시 시작하므로 이 값이 없으면
     * 두 번째 세트가 첫 세트와 같은 문제로 취급돼 통째로 건너뛰어진다.
     * 저장 전 rejectIfInconsistent 로 파일 전체를 검사하고, 본문이 유형과 안 맞으면 아무것도 저장하지 않는다.
     */
    @Transactional
    public ImportResult importQuizSet(CompetitionQuizImportDto dto, String setKey) {
        if (setKey == null || !SET_KEY.matcher(setKey).matches()) {
            throw new IllegalArgumentException("quizSet 은 영문·숫자·-_ 1~30자여야 합니다: " + setKey);
        }
        String langCode = dto.getLanguage();
        int savedDetail = 0;
        int savedContent = 0;

        rejectIfInconsistent(dto, setKey);

        for (LevelDto levelDto : dto.getLevels()) {
            for (QuestionDto q : levelDto.getQuestions()) {

                CompetitionQuizDetail quizDetail = quizDetailRepository
                        .findBySetKeyAndLevelAndOriginId(setKey, levelDto.getLevel(), q.getId())
                        .orElseGet(() -> {
                            CompetitionQuizDetail newDetail = new CompetitionQuizDetail();
                            newDetail.setSetKey(setKey);
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

        log.info("컴피티션 문제 import 완료 - set: {}, lang: {}, quizDetail 신규: {}, quizContent 신규: {}",
                setKey, langCode, savedDetail, savedContent);

        return new ImportResult(savedDetail, savedContent);
    }

    /**
     * 저장 전 검증.
     * 거절(400, 아무것도 저장 안 함): 파일 안에서 type 과 본문 모양이 안 맞는 문항 — multiple_choice 는 options,
     *   word_arrange 는 tiles 필수, subjective 는 둘 다 없어야 한다. 본문이 비면 어떤 방식으로도 그릴 수 없다.
     * 경고만: 이미 있는 (level, id) 의 quiz_type 과 파일의 type 이 다른 경우. 실제 언어별 파일은 같은 id 가 같은
     *   문제가 아니라(en↔ja 87건 상이, 2026-09-20 확인) 거절하면 정상 파일도 못 넣는다. 출제는 본문 모양으로
     *   유형을 정하므로 detail.quiz_type 은 "첫 언어의 유형" 참고값일 뿐 동작에 쓰이지 않는다.
     */
    void rejectIfInconsistent(CompetitionQuizImportDto dto, String setKey) {
        List<String> problems = new ArrayList<>();
        List<String> typeDiffs = new ArrayList<>();

        for (LevelDto levelDto : dto.getLevels()) {
            for (QuestionDto q : levelDto.getQuestions()) {
                String where = String.format("level=%d id=%d", levelDto.getLevel(), q.getId());
                String type = q.getType();

                Optional<CompetitionQuizDetail> existing =
                        quizDetailRepository.findBySetKeyAndLevelAndOriginId(setKey, levelDto.getLevel(), q.getId());
                if (existing.isPresent() && !existing.get().getQuizType().equals(type)) {
                    typeDiffs.add(String.format("%s: 기존 %s ↔ 파일 %s", where, existing.get().getQuizType(), type));
                }

                boolean hasOptions = q.getOptions() != null && !q.getOptions().isEmpty();
                boolean hasTiles = q.getTiles() != null && !q.getTiles().isEmpty();
                String shapeProblem = switch (type == null ? "" : type) {
                    case "multiple_choice" -> hasOptions ? null : "multiple_choice 인데 options 없음";
                    case "word_arrange" -> hasTiles ? null : "word_arrange 인데 tiles 없음";
                    case "subjective" -> (hasOptions || hasTiles) ? "subjective 인데 options/tiles 있음" : null;
                    default -> "알 수 없는 type: " + type;
                };
                if (shapeProblem != null) {
                    problems.add(where + ": " + shapeProblem);
                }
            }
        }

        if (!typeDiffs.isEmpty()) {
            log.warn("컴피티션 문제 import - set: {}, lang: {}, 기존 detail 과 유형이 다른 문항 {}건 (본문 기준 출제라 저장은 진행): {}",
                    setKey, dto.getLanguage(), typeDiffs.size(), typeDiffs);
        }
        if (!problems.isEmpty()) {
            log.warn("컴피티션 문제 import 거절 - set: {}, lang: {}, 문제 {}건: {}", setKey, dto.getLanguage(), problems.size(), problems);
            throw new IllegalArgumentException(String.format(
                    "유형과 본문이 맞지 않는 문항 %d건 — multiple_choice 는 options, word_arrange 는 tiles 가 있어야 하고 subjective 는 둘 다 없어야 합니다: %s",
                    problems.size(), String.join("; ", problems)));
        }
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