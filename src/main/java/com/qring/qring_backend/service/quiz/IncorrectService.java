package com.qring.qring_backend.service.quiz;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.domain.competition.CompetitionQuizContent;
import com.qring.qring_backend.domain.competition.CompetitionQuizContentRepository;
import com.qring.qring_backend.domain.competition.CompetitionQuizTypeUtil;
import com.qring.qring_backend.domain.competition.CompetitionWrongAnswer;
import com.qring.qring_backend.domain.competition.CompetitionWrongAnswerRepository;
import com.qring.qring_backend.domain.quiz.QuizContent;
import com.qring.qring_backend.domain.quiz.QuizContentRepository;
import com.qring.qring_backend.domain.quiz.WrongAnswerRepository;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.domain.user.UserAssetHistory.SourceType;
import com.qring.qring_backend.dto.quiz.IncorrectResponseDto;
import com.qring.qring_backend.dto.quiz.IncorrectResultRequestDto;
import com.qring.qring_backend.dto.quiz.IncorrectResultResponseDto;
import com.qring.qring_backend.dto.quiz.IncorrectRetryResponseDto;
import com.qring.qring_backend.service.user.UserPointService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class IncorrectService {

        static final int RETRY_CORRECT_POINTS = 3;
        static final int RETRY_WRONG_POINTS = 1;

        private final WrongAnswerRepository wrongAnswerRepository;
        private final CompetitionWrongAnswerRepository competitionWrongAnswerRepository;
        private final QuizContentRepository quizContentRepository;
        private final CompetitionQuizContentRepository competitionQuizContentRepository;
        private final UserPointService userPointService;
        private final UserRepository userRepository;

        /** 오답노트 목록 — 스토리(스토리+레벨 단위) + 컴피티션(레벨 단위) 통합, 최근 오답 순. */
        public IncorrectResponseDto getWrongAnswers(Long userId) {
                String langCode = getUserLanguage(userId);
                LocalDateTime cutoff = LocalDate.now().minusDays(7).atStartOfDay();

                List<IncorrectResponseDto.WrongAnswerItem> storyItems = wrongAnswerRepository
                                .findWrongAnswerSummaryByUserIdAndLangCode(userId, langCode, cutoff);

                List<IncorrectResponseDto.WrongAnswerItem> competitionItems = competitionWrongAnswerRepository
                                .findWrongAnswerSummaryByUserIdAndLangCode(userId, langCode, cutoff);

                List<IncorrectResponseDto.WrongAnswerItem> merged = Stream
                                .concat(storyItems.stream(), competitionItems.stream())
                                .sorted(Comparator.comparing(IncorrectResponseDto.WrongAnswerItem::getLatestWrongAt)
                                                .reversed())
                                .collect(Collectors.toList());

                return new IncorrectResponseDto(merged);
        }

        /**
         * 재풀이 문제 조회 — 목록과 같은 스코프(현재 학습 언어, 7일 이내)로만 내려준다.
         * STORY: groupId=contentId, level=difficulty (null 이면 전체 레벨).
         * COMPETITION: groupId=매치 레벨, level 파라미터는 쓰지 않는다.
         */
        public IncorrectRetryResponseDto getIncorrectQuizzes(Long userId, String sourceType, Long groupId,
                        Integer level) {
                String langCode = getUserLanguage(userId);
                LocalDateTime cutoff = LocalDate.now().minusDays(7).atStartOfDay();

                if ("COMPETITION".equals(sourceType)) {
                        List<CompetitionWrongAnswer> wrongAnswers = competitionWrongAnswerRepository
                                        .findByUserIdAndLevelAndLangCode(userId, groupId.intValue(), langCode, cutoff);

                        List<IncorrectRetryResponseDto.IncorrectQuizDto> quizzes = wrongAnswers.stream()
                                        .map(this::toIncorrectQuizDto)
                                        .collect(Collectors.toList());

                        return new IncorrectRetryResponseDto(quizzes);
                }

                List<IncorrectRetryResponseDto.IncorrectQuizDto> quizzes = wrongAnswerRepository
                                .findIncorrectQuizContentsByUserIdAndContentId(userId, groupId, langCode, level, cutoff)
                                .stream()
                                .map(qc -> toStoryQuizDto(qc, qc.getQuizDetail().getDifficulty()))
                                .collect(Collectors.toList());
                return new IncorrectRetryResponseDto(quizzes);
        }

        private String getUserLanguage(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
                return user.getLanguage();
        }

        /**
         * wa.sourceType(원본 출처)에 따라 실제 문제 본문을 다른 테이블에서 조회해 통일된 DTO로 변환.
         * 레벨은 원본과 상관없이 그 매치의 레벨(wa.level)로 표시한다 — 오답노트 묶음 기준과 동일.
         */
        private IncorrectRetryResponseDto.IncorrectQuizDto toIncorrectQuizDto(CompetitionWrongAnswer wa) {
                if (wa.getSourceType() == CompetitionWrongAnswer.SourceType.STORY) {
                        QuizContent qc = quizContentRepository.findById(wa.getQuizContentId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "스토리 문제를 찾을 수 없습니다: " + wa.getQuizContentId()));
                        return toStoryQuizDto(qc, wa.getLevel());
                }

                CompetitionQuizContent qc = competitionQuizContentRepository.findById(wa.getQuizContentId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "컴피티션 문제를 찾을 수 없습니다: " + wa.getQuizContentId()));
                return new IncorrectRetryResponseDto.IncorrectQuizDto(
                                qc.getQuizContentId(), qc.getQuestion(), qc.getOptions(), null,
                                qc.getAnswer(), CompetitionQuizTypeUtil.effectiveType(qc), "COMPETITION",
                                qc.getKorean(), qc.getTiles(), qc.getAnswerTiles(), qc.getDistractorTiles(),
                                wa.getLevel());
        }

        /**
         * 스토리 원본 문제 -> 재풀이 DTO. 스토리 오답노트와 컴피티션 오답노트(STORY 원본) 양쪽에서 쓴다.
         * quizType 은 detail.quiz_type 이 아니라 본문(options) 기준 —
         * options 가 비어 있는데 detail 이 객관식 계열이면 프론트가 보기 없는 객관식을 그리기 때문.
         */
        private IncorrectRetryResponseDto.IncorrectQuizDto toStoryQuizDto(QuizContent qc, Integer level) {
                return new IncorrectRetryResponseDto.IncorrectQuizDto(
                                qc.getQuizContentId(), qc.getQuestion(), qc.getOptions(), qc.getHint(),
                                qc.getCorrectAnswer(),
                                CompetitionQuizTypeUtil.effectiveStoryType(
                                                qc.getQuizDetail().getQuizType(), qc.getOptions()),
                                "STORY", level);
        }

        public IncorrectResultResponseDto saveIncorrectResult(Long userId, IncorrectResultRequestDto request) {
                int totalPoint = 0;
                boolean isCompetition = "COMPETITION".equals(request.getSourceType());

                for (IncorrectResultRequestDto.QuizResultDto result : request.getResults()) {
                        if (isCompetition) {
                                CompetitionWrongAnswer.SourceType originType = CompetitionWrongAnswer.SourceType
                                                .valueOf(result.getOriginSourceType());
                                competitionWrongAnswerRepository.deleteByUserIdAndQuizContentIdAndSourceType(
                                                userId, result.getQuizContentId(), originType);
                        } else {
                                wrongAnswerRepository.deleteByUserIdAndQuizContentId(userId, result.getQuizContentId());
                        }
                        totalPoint += result.isCorrect() ? RETRY_CORRECT_POINTS : RETRY_WRONG_POINTS;
                }

                userPointService.earn(userId, totalPoint, SourceType.INCORRECT_RETRY, request.getContentId());

                return new IncorrectResultResponseDto(totalPoint);
        }
}