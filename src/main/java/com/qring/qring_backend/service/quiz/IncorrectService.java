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

        public IncorrectResponseDto getWrongAnswers(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
                String langCode = user.getLanguage();
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

        public IncorrectRetryResponseDto getIncorrectQuizzes(Long userId, String sourceType, Long groupId) {
                LocalDateTime cutoff = LocalDate.now().minusDays(7).atStartOfDay();

                if ("COMPETITION".equals(sourceType)) {
                        List<CompetitionWrongAnswer> wrongAnswers = competitionWrongAnswerRepository
                                        .findByUserIdAndLevel(userId, groupId.intValue(), cutoff);

                        List<IncorrectRetryResponseDto.IncorrectQuizDto> quizzes = wrongAnswers.stream()
                                        .map(this::toIncorrectQuizDto)
                                        .collect(Collectors.toList());

                        return new IncorrectRetryResponseDto(quizzes);
                }

                List<IncorrectRetryResponseDto.IncorrectQuizDto> quizzes = wrongAnswerRepository
                                .findIncorrectQuizzesByUserIdAndContentId(userId, groupId, cutoff);
                return new IncorrectRetryResponseDto(quizzes);
        }

        /** wa.sourceType(원본 출처)에 따라 실제 문제 본문을 다른 테이블에서 조회해 통일된 DTO로 변환. */
        private IncorrectRetryResponseDto.IncorrectQuizDto toIncorrectQuizDto(CompetitionWrongAnswer wa) {
                if (wa.getSourceType() == CompetitionWrongAnswer.SourceType.STORY) {
                        QuizContent qc = quizContentRepository.findById(wa.getQuizContentId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "스토리 문제를 찾을 수 없습니다: " + wa.getQuizContentId()));
                        String rawType = qc.getQuizDetail().getQuizType();
                        String quizType = "fill_in_blank".equals(rawType) ? "subjective" : rawType;
                        return new IncorrectRetryResponseDto.IncorrectQuizDto(
                                        qc.getQuizContentId(), qc.getQuestion(), qc.getOptions(), qc.getHint(),
                                        qc.getCorrectAnswer(), quizType, "STORY",
                                        null, null, null, null);
                }

                CompetitionQuizContent qc = competitionQuizContentRepository.findById(wa.getQuizContentId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "컴피티션 문제를 찾을 수 없습니다: " + wa.getQuizContentId()));
                return new IncorrectRetryResponseDto.IncorrectQuizDto(
                                qc.getQuizContentId(), qc.getQuestion(), qc.getOptions(), null,
                                qc.getAnswer(), CompetitionQuizTypeUtil.effectiveType(qc), "COMPETITION",
                                qc.getKorean(), qc.getTiles(), qc.getAnswerTiles(), qc.getDistractorTiles());
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