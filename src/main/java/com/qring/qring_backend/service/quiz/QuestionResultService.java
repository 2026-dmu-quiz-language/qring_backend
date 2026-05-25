package com.qring.qring_backend.service.quiz;

import com.qring.qring_backend.domain.quiz.QuizDetail;
import com.qring.qring_backend.domain.quiz.QuizDetailRepository;
import com.qring.qring_backend.domain.quiz.QuizResult;
import com.qring.qring_backend.domain.quiz.QuizResultRepository;
import com.qring.qring_backend.domain.quiz.QuizService;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.dto.quiz.QuestionResultRequestDto;
import com.qring.qring_backend.dto.quiz.QuestionResultResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuestionResultService {

    private final QuizService quizService;
    private final QuizDetailRepository quizDetailRepository;
    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;

    @Transactional
    public QuestionResultResponseDto saveResult(Long userId, QuestionResultRequestDto request) {

        // 퀴즈 정보 조회
        QuizDetail quizDetail = quizDetailRepository.findById(request.getQuizId())
                .orElseThrow(() -> new IllegalArgumentException("퀴즈를 찾을 수 없습니다."));

        // 점수 계산 (정답인 경우만, 오답은 0점)
        int score = 0;
        if (request.isCorrect()) {
            score = quizService.getCalculatedScore(
                    quizDetail.getDifficulty(),
                    request.getAttemptCount(),
                    request.isHintUsed()
            );
        }

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 결과 저장
        QuizResult quizResult = QuizResult.builder()
                .user(user)
                .contentId(quizDetail.getContent().getContentId())
                .scriptId(quizDetail.getScript().getScriptId())
                .difficulty(quizDetail.getDifficulty())
                .attemptCount(request.getAttemptCount())
                .hintUsed(request.isHintUsed())
                .score(score)
                .build();

        quizResultRepository.save(quizResult);

        // 정답 횟수 조회
        int correctCount = quizResultRepository.countCorrectByUserIdAndContentId(
                userId, quizDetail.getContent().getContentId());

        return new QuestionResultResponseDto(score, correctCount);
    }
}
