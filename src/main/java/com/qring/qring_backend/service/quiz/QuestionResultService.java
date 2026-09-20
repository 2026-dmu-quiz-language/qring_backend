package com.qring.qring_backend.service.quiz;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.domain.content.Content;
import com.qring.qring_backend.domain.quiz.QuizContentRepository;
import com.qring.qring_backend.domain.quiz.QuizDetail;
import com.qring.qring_backend.domain.quiz.QuizDetailRepository;
import com.qring.qring_backend.domain.quiz.QuizResult;
import com.qring.qring_backend.domain.quiz.QuizResultRepository;
import com.qring.qring_backend.domain.quiz.QuizService;
import com.qring.qring_backend.domain.quiz.StoryProgress;
import com.qring.qring_backend.domain.quiz.StoryProgressRepository;
import com.qring.qring_backend.domain.quiz.WrongAnswer;
import com.qring.qring_backend.domain.quiz.WrongAnswerRepository;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.domain.user.UserAssetHistory.SourceType;
import com.qring.qring_backend.domain.user.UserStudyLog;
import com.qring.qring_backend.domain.user.UserStudyLogRepository;
import com.qring.qring_backend.domain.user.Userprogress;
import com.qring.qring_backend.domain.user.UserprogressRepository;
import com.qring.qring_backend.dto.quiz.QuestionResultRequestDto;
import com.qring.qring_backend.dto.quiz.QuestionResultRequestDto.QuizResultDto;
import com.qring.qring_backend.dto.quiz.QuestionResultResponseDto;
import com.qring.qring_backend.service.user.UserPointService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionResultService {

    /** 스토리 학습 완료 시 지급되는 포인트의 상한 (포인트표 확정값). */
    private static final int STORY_LEARNING_POINT_CAP = 100;

    private final QuizService quizService;
    private final QuizDetailRepository quizDetailRepository;
    private final QuizResultRepository quizResultRepository;
    private final QuizContentRepository quizContentRepository;
    private final WrongAnswerRepository wrongAnswerRepository;
    private final UserPointService userPointService;
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

        // 이 스토리를 이미 완료한 적 있는지 먼저 확인 (재학습 시 포인트 재지급 방지)
        boolean alreadyCompleted = false;
        if (request.getResults() != null && !request.getResults().isEmpty()) {
            QuizDetail firstQuizDetail = quizDetailRepository.findById(request.getResults().get(0).getQuizId())
                    .orElseThrow(() -> new IllegalArgumentException("퀴즈를 찾을 수 없습니다."));
            content = firstQuizDetail.getContent();
            if (language != null) {
                alreadyCompleted = storyProgressRepository
                        .findByUserIdAndContentIdAndLanguage(userId, content.getContentId(), language)
                        .map(StoryProgress::getIsCompleted)
                        .orElse(false);
            }
        }

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
                    .build());

            // user_study_log 저장
            UserStudyLog studyLog = new UserStudyLog();
            studyLog.setUser(user);
            studyLog.setQuiz(quizDetail);
            studyLog.setUserResponse(result.getLastAnswer());
            studyLog.setIsCorrect(result.isCorrect());
            studyLog.setLangCode(language);
            userStudyLogRepository.save(studyLog);

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

        // 스토리 학습 포인트 지급: 최초 완료(재학습 아님)일 때만, 최대 100p로 캡 (포인트표 확정값)
        // 퀴즈 개별 점수(quiz_result.score, totalScore)는 캡 없이 그대로 기록/응답하고,
        // 실제로 유저 포인트에 적립되는 금액만 상한을 건다.
        if (!alreadyCompleted) {
            int storyPoints = Math.min(totalScore, STORY_LEARNING_POINT_CAP);
            userPointService.earn(userId, storyPoints, SourceType.STORY_LEARNING,
                    content != null ? content.getContentId() : null);
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
                        p.setLanguage(language);
                        return p;
                    });

            // 해당 언어의 첫 스토리 완료 시 30점 추가 부여 (언어 추가 30p — 언어 변경 후 그 언어로 처음 완료한 스토리에 지급)
            if (progress.getProgressRate() == null || progress.getProgressRate() < 100) {
                if (language != null) {
                    long completed = userprogressRepository.countCompletedStories(userId, language);
                    if (completed == 0) {
                        userPointService.earn(userId, 30, SourceType.STORY_COMPLETE_BONUS, finalContent.getContentId());
                    }
                }
            }

            progress.setProgressRate(100);
            progress.setLanguage(language);
            progress.setUpdatedAt(LocalDateTime.now());
            userprogressRepository.save(progress);
        }

        return new QuestionResultResponseDto(totalScore, correctCount);
    }
}