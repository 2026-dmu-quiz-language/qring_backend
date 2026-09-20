package com.qring.qring_backend.service.quiz;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qring.qring_backend.auth.repository.UserRepository;
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
    private final UserPointService userPointService;
    private final UserRepository userRepository;

    public IncorrectResponseDto getWrongAnswers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
        String langCode = user.getLanguage();

        LocalDateTime cutoff = LocalDate.now().minusDays(7).atStartOfDay();

        List<IncorrectResponseDto.WrongAnswerSummary> list = wrongAnswerRepository
                .findWrongAnswerSummaryByUserIdAndLangCode(userId, langCode, cutoff);
        return new IncorrectResponseDto(list);
    }
    
    public IncorrectRetryResponseDto getIncorrectQuizzes(Long userId, Long contentId) {
        LocalDateTime cutoff = LocalDate.now().minusDays(7).atStartOfDay();

        List<IncorrectRetryResponseDto.IncorrectQuizDto> quizzes = wrongAnswerRepository
                .findIncorrectQuizzesByUserIdAndContentId(userId, contentId, cutoff);
        return new IncorrectRetryResponseDto(quizzes);
    }
    
    public IncorrectResultResponseDto saveIncorrectResult(Long userId, IncorrectResultRequestDto request) {
        int totalPoint = 0;

        for (IncorrectResultRequestDto.QuizResultDto result : request.getResults()) {
            // 정답이든 오답이든(다시 틀리든) 한 번 다시 풀었으므로 오답 목록에서 무조건 삭제
            wrongAnswerRepository.deleteByUserIdAndQuizContentId(userId, result.getQuizContentId());
            totalPoint += result.isCorrect() ? RETRY_CORRECT_POINTS : RETRY_WRONG_POINTS;
        }

        // 문항마다 UPDATE 하지 않고 합산해 한 번에 적립 + 히스토리 한 건 (reference = content_id)
        userPointService.earn(userId, totalPoint, SourceType.INCORRECT_RETRY, request.getContentId());

        return new IncorrectResultResponseDto(totalPoint);
    }
}