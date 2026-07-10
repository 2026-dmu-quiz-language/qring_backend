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
import com.qring.qring_backend.domain.quiz.StoryProgress;
import com.qring.qring_backend.domain.quiz.StoryProgressRepository;
import com.qring.qring_backend.domain.quiz.QuizContent;
import com.qring.qring_backend.domain.quiz.QuizContentRepository;
import com.qring.qring_backend.domain.quiz.WrongAnswer;
import com.qring.qring_backend.domain.quiz.WrongAnswerRepository;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.domain.user.UserAssetRepository;
import com.qring.qring_backend.domain.user.UserStudyLog;
import com.qring.qring_backend.domain.user.UserStudyLogRepository;
import com.qring.qring_backend.domain.user.Userprogress;
import com.qring.qring_backend.domain.user.UserprogressRepository;
import com.qring.qring_backend.dto.quiz.QuestionResultRequestDto;
import com.qring.qring_backend.dto.quiz.QuestionResultRequestDto.QuizResultDto;
import com.qring.qring_backend.dto.quiz.QuestionResultResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionResultService {

    private final QuizService quizService;
    private final QuizDetailRepository quizDetailRepository;
    private final QuizResultRepository quizResultRepository;
    private final QuizContentRepository quizContentRepository;
    private final WrongAnswerRepository wrongAnswerRepository;
    private final UserAssetRepository userAssetRepository;
    private final UserStudyLogRepository userStudyLogRepository;
    private final UserprogressRepository userprogressRepository;
    private final UserRepository userRepository;
    private final StoryProgressRepository storyProgressRepository;

    @Transactional
    public QuestionResultResponseDto saveResults(Long userId, QuestionResultRequestDto request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        String language = user.getLanguage();

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
                        result.isHintUsed());
                correctCount++;
            } else {
                score = quizService.getCalculatedScore(
                        quizDetail.getDifficulty(),
                        4,
                        false);
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
                    .langCode(language)
                    .level(quizDetail.getDifficulty())
                    .build());

            // user_study_log 저장
            UserStudyLog studyLog = new UserStudyLog();
            studyLog.setUser(user);
            studyLog.setQuiz(quizDetail);
            studyLog.setUserResponse(result.getLastAnswer());
            studyLog.setIsCorrect(result.isCorrect());
            studyLog.setLangCode(language);
            userStudyLogRepository.save(studyLog);

            // 포인트 적립 (정답: 3점, 오답: 1점)
            int pointsToAdd = result.isCorrect() ? 3 : 1;
            userAssetRepository.addPoints(userId, pointsToAdd);

            // wrong_answer 처리: 유저 언어 + quizId로 quiz_content_id 조회
            if (language != null) {
                quizContentRepository.findByQuizIdAndLangCode(result.getQuizId(), language)
                        .ifPresent(quizContent -> {
                            Long quizContentId = quizContent.getQuizContentId();
                            if (result.isCorrect()) {
                                // 정답이면 오답 목록에서 삭제
                                wrongAnswerRepository.deleteByUserIdAndQuizContentId(userId, quizContentId);
                            } else {
                                // 오답이면 wrong_answer에 저장 (이미 있으면 중복 저장 안 함)
                                boolean alreadyExists = wrongAnswerRepository
                                        .findByUserIdAndQuizContentId(userId, quizContentId)
                                        .isPresent();
                                if (!alreadyExists) {
                                    WrongAnswer wa = new WrongAnswer();
                                    wa.setUserId(userId);
                                    wa.setQuizContentId(quizContentId);
                                    wa.setLevel(quizDetail.getDifficulty());
                                    wa.setStoryName(quizDetail.getContent().getTitle());
                                    wa.setContentId(quizDetail.getContent().getContentId());
                                    wrongAnswerRepository.save(wa);
                                }
                            }
                        });
            }
        }

        // story_progress 저장 (언어별 스토리 완료 처리)
        if (content != null && language != null) {
            Content finalContent = content;
            StoryProgress sp = storyProgressRepository
                    .findByUserIdAndContentIdAndLanguage(userId, finalContent.getContentId(), language)
                    .orElseGet(() -> {
                        StoryProgress newSp = new StoryProgress();
                        newSp.setUserId(userId);
                        newSp.setContentId(finalContent.getContentId());
                        newSp.setLanguage(language);
                        newSp.setLevel(user.getLevelCode());
                        return newSp;
                    });
            sp.setIsCompleted(true);
            sp.setCompletedAt(LocalDateTime.now());
            storyProgressRepository.save(sp);
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