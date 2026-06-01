package com.qring.qring_backend.service.quiz;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.domain.content.Content;
import com.qring.qring_backend.domain.quiz.QuizDetail;
import com.qring.qring_backend.domain.quiz.QuizDetailRepository;
import com.qring.qring_backend.domain.quiz.QuizResult;
import com.qring.qring_backend.domain.quiz.QuizResultRepository;
import com.qring.qring_backend.domain.quiz.QuizService;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.domain.user.UserStudyLog;
import com.qring.qring_backend.domain.user.UserStudyLogRepository;
import com.qring.qring_backend.domain.user.Userprogress;
import com.qring.qring_backend.domain.user.UserprogressRepository;
import com.qring.qring_backend.dto.quiz.QuestionResultRequestDto;
import com.qring.qring_backend.dto.quiz.QuestionResultRequestDto.QuizResultDto;
import com.qring.qring_backend.dto.quiz.QuestionResultResponseDto;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QuestionResultService {

    private final QuizService quizService;
    private final QuizDetailRepository quizDetailRepository;
    private final QuizResultRepository quizResultRepository;
    private final UserStudyLogRepository userStudyLogRepository;
    private final UserprogressRepository userprogressRepository;
    private final UserRepository userRepository;

    @Transactional
    public QuestionResultResponseDto saveResults(Long userId, QuestionResultRequestDto request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        int totalScore = 0;
        int correctCount = 0;
        Content content = null;

        for (QuizResultDto result : request.getResults()) {

            QuizDetail quizDetail = quizDetailRepository.findById(result.getQuizId())
                    .orElseThrow(() -> new IllegalArgumentException("퀴즈를 찾을 수 없습니다."));

            if (content == null) {
                content = quizDetail.getContent();
            }

            // 점수 계산 (정답: 실제 점수, 오답: 난이도별 기본 점수)
            int score;
            if (result.isCorrect()) {
                score = quizService.getCalculatedScore(
                        quizDetail.getDifficulty(),
                        result.getAttemptCount(),
                        result.isHintUsed()
                );
                correctCount++;
            } else {
                score = quizService.getCalculatedScore(
                        quizDetail.getDifficulty(),
                        4,
                        false
                );
            }

            totalScore += score;

            // quiz_result 저장
            quizResultRepository.save(QuizResult.builder()
                    .user(user)
                    .contentId(quizDetail.getContent().getContentId())
                    .scriptId(quizDetail.getScript().getScriptId())
                    .difficulty(quizDetail.getDifficulty())
                    .attemptCount(result.getAttemptCount())
                    .hintUsed(result.isHintUsed())
                    .score(score)
                    .build());

            // user_study_log 저장
            UserStudyLog studyLog = new UserStudyLog();
            studyLog.setUser(user);
            studyLog.setQuiz(quizDetail);
            studyLog.setUserResponse(result.getLastAnswer());
            studyLog.setIsCorrect(result.isCorrect());
            userStudyLogRepository.save(studyLog);
        }

        // user_progress 저장 (콘텐츠 완료 처리)
        if (content != null) {
            Content finalContent = content;
            Userprogress progress = userprogressRepository
                    .findByUserUserIdAndContentContentId(userId, content.getContentId())
                    .orElseGet(() -> {
                        Userprogress p = new Userprogress();
                        p.setUser(user);
                        p.setContent(finalContent);
                        return p;
                    });
            progress.setProgressRate(100);
            progress.setUpdatedAt(LocalDateTime.now());
            userprogressRepository.save(progress);
        }

        return new QuestionResultResponseDto(totalScore, correctCount);
    }
}