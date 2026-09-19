package com.qring.qring_backend.service.quiz;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.domain.quiz.WrongAnswerRepository;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.domain.user.UserAssetRepository;
import com.qring.qring_backend.dto.quiz.IncorrectResponseDto;
import com.qring.qring_backend.dto.quiz.IncorrectResultRequestDto;
import com.qring.qring_backend.dto.quiz.IncorrectResultResponseDto;
import com.qring.qring_backend.dto.quiz.IncorrectRetryResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class IncorrectService {

    private final WrongAnswerRepository wrongAnswerRepository;
    private final UserAssetRepository userAssetRepository;
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

            if (result.isCorrect()) {
                // 정답이면 3점
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