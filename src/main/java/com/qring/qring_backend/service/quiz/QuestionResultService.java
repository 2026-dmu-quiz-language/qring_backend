package com.qring.qring_backend.service.quiz;

import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.domain.quiz.QuizDetail;
import com.qring.qring_backend.domain.quiz.QuizDetailRepository;
import com.qring.qring_backend.domain.quiz.QuizResult;
import com.qring.qring_backend.domain.quiz.QuizResultRepository;
import com.qring.qring_backend.domain.quiz.QuizService;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.dto.quiz.QuestionResultRequestDto;
import com.qring.qring_backend.dto.quiz.QuestionResultRequestDto.QuizResultDto;
import com.qring.qring_backend.dto.quiz.QuestionResultResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionResultService {

    private final QuizService quizService;
    private final QuizDetailRepository quizDetailRepository;
    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;

    @Transactional
    public QuestionResultResponseDto saveResults(Long userId, QuestionResultRequestDto request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        int totalScore = 0;
        int correctCount = 0;

        for (QuizResultDto result : request.getResults()) {

            QuizDetail quizDetail = quizDetailRepository.findById(result.getQuizId())
                    .orElseThrow(() -> new IllegalArgumentException("퀴즈를 찾을 수 없습니다."));

            // 점수 계산 (정답인 경우만)
            int score = 0;
            if (result.isCorrect()) {
                score = quizService.getCalculatedScore(
                        quizDetail.getDifficulty(),
                        result.getAttemptCount(),
                        result.isHintUsed()
                );
                correctCount++;
            }

            totalScore += score;

            // 결과 저장
            QuizResult quizResult = QuizResult.builder()
                    .user(user)
                    .contentId(quizDetail.getContent().getContentId())
                    .scriptId(quizDetail.getScript().getScriptId())
                    .difficulty(quizDetail.getDifficulty())
                    .attemptCount(result.getAttemptCount())
                    .hintUsed(result.isHintUsed())
                    .score(score)
                    .build();

            quizResultRepository.save(quizResult);
        }

        return new QuestionResultResponseDto(totalScore, correctCount);
    }
}