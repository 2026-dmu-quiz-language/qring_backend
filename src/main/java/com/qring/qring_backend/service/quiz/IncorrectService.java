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
import com.qring.qring_backend.domain.competition.CompetitionQuizTypeUtil;
import com.qring.qring_backend.domain.competition.CompetitionWrongAnswer;
import com.qring.qring_backend.domain.competition.CompetitionWrongAnswerRepository;
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

    /** 오답 노트 재풀이 포인트: 정답 3, 오답(다시 틀림) 1. 요청 단위로 합산해 히스토리 한 건으로 남긴다. */
    static final int RETRY_CORRECT_POINTS = 3;
    static final int RETRY_WRONG_POINTS = 1;

    private final WrongAnswerRepository wrongAnswerRepository;
    private final CompetitionWrongAnswerRepository competitionWrongAnswerRepository;
    private final UserPointService userPointService;
    private final UserRepository userRepository;

    /** 오답노트 목록 — STORY + COMPETITION 통합, 최근 오답 순 정렬. */
    public IncorrectResponseDto getWrongAnswers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
        String langCode = user.getLanguage();

        LocalDateTime cutoff = LocalDate.now().minusDays(7).atStartOfDay();

        List<IncorrectResponseDto.WrongAnswerItem> storyItems = wrongAnswerRepository
                .findWrongAnswerSummaryByUserIdAndLangCode(userId, langCode, cutoff);

        List<IncorrectResponseDto.WrongAnswerItem> competitionItems = competitionWrongAnswerRepository
                .findWrongAnswerSummaryByUserIdAndLangCode(userId, langCode, cutoff);

        List<IncorrectResponseDto.WrongAnswerItem> merged = Stream.concat(storyItems.stream(), competitionItems.stream())
                .sorted(Comparator.comparing(IncorrectResponseDto.WrongAnswerItem::getLatestWrongAt).reversed())
                .collect(Collectors.toList());

        return new IncorrectResponseDto(merged);
    }

    /**
     * 오답 문제 목록 조회 (다시 풀기 화면).
     * sourceType 이 COMPETITION 이면 groupId 는 level, STORY 면 content_id 로 해석한다.
     */
    public IncorrectRetryResponseDto getIncorrectQuizzes(Long userId, String sourceType, Long groupId) {
        LocalDateTime cutoff = LocalDate.now().minusDays(7).atStartOfDay();

        if ("COMPETITION".equals(sourceType)) {
            List<CompetitionWrongAnswer> wrongAnswers = competitionWrongAnswerRepository
                    .findByUserIdAndLevel(userId, groupId.intValue(), cutoff);

            List<IncorrectRetryResponseDto.IncorrectQuizDto> quizzes = wrongAnswers.stream()
                    .map(wa -> {
                        CompetitionQuizContent qc = wa.getQuizContent();
                        return new IncorrectRetryResponseDto.IncorrectQuizDto(
                                qc.getQuizContentId(),
                                qc.getQuestion(),
                                qc.getOptions(),
                                null, // 컴피티션 문제엔 hint 필드가 없음
                                qc.getAnswer(),
                                CompetitionQuizTypeUtil.effectiveType(qc),
                                "COMPETITION"
                        );
                    })
                    .collect(Collectors.toList());

            return new IncorrectRetryResponseDto(quizzes);
        }

        List<IncorrectRetryResponseDto.IncorrectQuizDto> quizzes = wrongAnswerRepository
                .findIncorrectQuizzesByUserIdAndContentId(userId, groupId, cutoff);
        return new IncorrectRetryResponseDto(quizzes);
    }

    /** 재풀이 결과 저장 — sourceType 에 따라 STORY/COMPETITION 오답 테이블에서 각각 삭제. */
    public IncorrectResultResponseDto saveIncorrectResult(Long userId, IncorrectResultRequestDto request) {
        int totalPoint = 0;
        boolean isCompetition = "COMPETITION".equals(request.getSourceType());

        for (IncorrectResultRequestDto.QuizResultDto result : request.getResults()) {
            if (isCompetition) {
                competitionWrongAnswerRepository
                        .deleteByUserIdAndQuizContentQuizContentId(userId, result.getQuizContentId());
            } else {
                wrongAnswerRepository.deleteByUserIdAndQuizContentId(userId, result.getQuizContentId());
            }
            totalPoint += result.isCorrect() ? RETRY_CORRECT_POINTS : RETRY_WRONG_POINTS;
        }

        // 문항마다 UPDATE 하지 않고 합산해 한 번에 적립 + 히스토리 한 건 (reference = content_id 또는 level)
        userPointService.earn(userId, totalPoint, SourceType.INCORRECT_RETRY, request.getContentId());

        return new IncorrectResultResponseDto(totalPoint);
    }
}