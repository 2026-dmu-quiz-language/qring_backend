package com.qring.qring_backend.service.quiz;

import com.qring.qring_backend.domain.quiz.WrongAnswerRepository;
import com.qring.qring_backend.domain.user.UserAssetRepository;
import com.qring.qring_backend.dto.quiz.IncorrectResponseDto;
import com.qring.qring_backend.dto.quiz.IncorrectResultRequestDto;
import com.qring.qring_backend.dto.quiz.IncorrectResultResponseDto;
import com.qring.qring_backend.dto.quiz.IncorrectRetryResponseDto;
import com.qring.qring_backend.domain.user.UserAssetRepository;
import com.qring.qring_backend.dto.quiz.IncorrectResultRequestDto;
import com.qring.qring_backend.dto.quiz.IncorrectResultResponseDto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IncorrectService {

    private final WrongAnswerRepository wrongAnswerRepository;
    private final UserAssetRepository userAssetRepository;

    public IncorrectResponseDto getWrongAnswers(Long userId) {
        List<IncorrectResponseDto.WrongAnswerSummary> list = wrongAnswerRepository
                .findWrongAnswerSummaryByUserId(userId);
        return new IncorrectResponseDto(list);
    }
    
    public IncorrectRetryResponseDto getIncorrectQuizzes(Long userId, Long contentId) {
        List<IncorrectRetryResponseDto.IncorrectQuizDto> quizzes = wrongAnswerRepository
                .findIncorrectQuizzesByUserIdAndContentId(userId, contentId);
        return new IncorrectRetryResponseDto(quizzes);
    }
    
    public IncorrectResultResponseDto saveIncorrectResult(Long userId, IncorrectResultRequestDto request) {
    int totalPoint = 0;

    for (IncorrectResultRequestDto.QuizResultDto result : request.getResults()) {
        if (result.isCorrect()) {
            // 정답이면 오답 목록에서 삭제 + 3점
            wrongAnswerRepository.deleteByUserIdAndQuizContentId(userId, result.getQuizContentId());
            userAssetRepository.addPoints(userId, 3);
            totalPoint += 3;
        } else {
            // 오답이면 1점
            userAssetRepository.addPoints(userId, 1);
            totalPoint += 1;
        }
    }

    return new IncorrectResultResponseDto(totalPoint);
}
}