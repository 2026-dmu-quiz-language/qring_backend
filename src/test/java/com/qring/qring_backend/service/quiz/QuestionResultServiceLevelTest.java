package com.qring.qring_backend.service.quiz;

import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.domain.content.Content;
import com.qring.qring_backend.domain.quiz.QuizContentRepository;
import com.qring.qring_backend.domain.quiz.QuizDetail;
import com.qring.qring_backend.domain.quiz.QuizDetailRepository;
import com.qring.qring_backend.domain.quiz.QuizResultRepository;
import com.qring.qring_backend.domain.quiz.QuizService;
import com.qring.qring_backend.domain.quiz.StoryProgress;
import com.qring.qring_backend.domain.quiz.StoryProgressRepository;
import com.qring.qring_backend.domain.quiz.WrongAnswerRepository;
import com.qring.qring_backend.domain.script.Script;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.domain.user.UserAssetHistory.SourceType;
import com.qring.qring_backend.domain.user.UserStudyLogRepository;
import com.qring.qring_backend.domain.user.UserprogressRepository;
import com.qring.qring_backend.dto.quiz.QuestionResultRequestDto;
import com.qring.qring_backend.service.user.StudyStreakService;
import com.qring.qring_backend.service.user.UserPointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 스토리 완료는 (언어, 레벨) 단위. DB 키 변경 없이 quiz_result(난이도·언어별 풀이 기록)로 판정한다:
 * 레벨 1 로 완료한 스토리를 레벨 2 문제로 풀면 새 완료(포인트 지급), 같은 레벨을 다시 풀면 재학습(포인트 없음).
 */
class QuestionResultServiceLevelTest {

    private static final long USER_ID = 19L;
    private static final long CONTENT_ID = 9L;

    private QuizDetailRepository quizDetailRepository;
    private QuizResultRepository quizResultRepository;
    private StoryProgressRepository storyProgressRepository;
    private UserprogressRepository userprogressRepository;
    private UserPointService userPointService;
    private QuestionResultService service;
    private User user;

    @BeforeEach
    void setUp() {
        UserRepository userRepository = mock(UserRepository.class);
        QuizService quizService = mock(QuizService.class);
        quizDetailRepository = mock(QuizDetailRepository.class);
        quizResultRepository = mock(QuizResultRepository.class);
        storyProgressRepository = mock(StoryProgressRepository.class);
        userprogressRepository = mock(UserprogressRepository.class);
        userPointService = mock(UserPointService.class);
        QuizContentRepository quizContentRepository = mock(QuizContentRepository.class);

        service = new QuestionResultService(quizService, quizDetailRepository, quizResultRepository,
                quizContentRepository, mock(WrongAnswerRepository.class), userPointService,
                mock(UserStudyLogRepository.class), userprogressRepository, userRepository, storyProgressRepository,
                mock(StudyStreakService.class));

        user = User.builder().userId(USER_ID).language("EN").levelCode(2).build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(quizService.getCalculatedScore(anyInt(), anyInt(), anyBoolean())).thenReturn(10);
        when(quizContentRepository.findByQuizIdAndLangCode(anyLong(), anyString())).thenReturn(Optional.empty());
        // 언어 첫 완료 보너스는 이 테스트의 관심사가 아니므로 이미 완료 스토리가 있는 것으로 둔다
        when(userprogressRepository.countCompletedStories(anyLong(), anyString())).thenReturn(1L);
        when(userprogressRepository.findByUserUserIdAndContentContentId(anyLong(), anyLong())).thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("레벨 1 완료 기록만 있을 때 레벨 2 문제를 풀면: 새 완료(포인트 지급), story_progress 의 레벨은 2 로 갱신")
    void higherLevel_isNewCompletion() {
        stubQuiz(100L, 2);
        when(quizResultRepository.existsByUserUserIdAndContentIdAndDifficultyAndLangCode(USER_ID, CONTENT_ID, 2, "EN"))
                .thenReturn(false);                                   // 레벨 2 풀이 기록 없음
        StoryProgress level1 = progress(1);                           // 마지막 완료는 레벨 1
        when(storyProgressRepository.findByUserIdAndContentIdAndLanguage(USER_ID, CONTENT_ID, "EN"))
                .thenReturn(Optional.of(level1));

        service.saveResults(USER_ID, request(100L));

        verify(userPointService).earn(eq(USER_ID), eq(10), eq(SourceType.STORY_LEARNING), eq(CONTENT_ID));
        ArgumentCaptor<StoryProgress> saved = ArgumentCaptor.forClass(StoryProgress.class);
        verify(storyProgressRepository).save(saved.capture());
        assertEquals(level1, saved.getValue());                       // 같은 row 재사용 (유니크 키 유지)
        assertEquals(2, saved.getValue().getLevel());
        assertTrue(saved.getValue().getIsCompleted());
    }

    @Test
    @DisplayName("같은 레벨을 다시 풀면(quiz_result 에 기록 있음): 재학습, 학습 포인트 없음")
    void sameLevel_isReplay() {
        stubQuiz(100L, 2);
        when(quizResultRepository.existsByUserUserIdAndContentIdAndDifficultyAndLangCode(USER_ID, CONTENT_ID, 2, "EN"))
                .thenReturn(true);
        when(storyProgressRepository.findByUserIdAndContentIdAndLanguage(USER_ID, CONTENT_ID, "EN"))
                .thenReturn(Optional.of(progress(2)));

        service.saveResults(USER_ID, request(100L));

        verify(userPointService, never()).earn(anyLong(), anyInt(), eq(SourceType.STORY_LEARNING), any());
    }

    @Test
    @DisplayName("옛 데이터 보조 판정: quiz_result 에 없어도 story_progress 의 마지막 완료 레벨이 같으면 재학습")
    void legacyFallback_storyProgressLevel() {
        stubQuiz(100L, 2);
        when(quizResultRepository.existsByUserUserIdAndContentIdAndDifficultyAndLangCode(USER_ID, CONTENT_ID, 2, "EN"))
                .thenReturn(false);
        when(storyProgressRepository.findByUserIdAndContentIdAndLanguage(USER_ID, CONTENT_ID, "EN"))
                .thenReturn(Optional.of(progress(2)));

        service.saveResults(USER_ID, request(100L));

        verify(userPointService, never()).earn(anyLong(), anyInt(), eq(SourceType.STORY_LEARNING), any());
    }

    @Test
    @DisplayName("완료 레벨은 사용자 설정이 아니라 실제로 푼 문제의 난이도를 따른다")
    void levelComesFromQuizDifficulty() {
        user.setLevelCode(3);        // 푸는 도중 레벨을 3 으로 바꿨어도
        stubQuiz(100L, 2);           // 푼 문제는 난이도 2
        when(quizResultRepository.existsByUserUserIdAndContentIdAndDifficultyAndLangCode(anyLong(), anyLong(), anyInt(), anyString()))
                .thenReturn(false);
        when(storyProgressRepository.findByUserIdAndContentIdAndLanguage(anyLong(), anyLong(), anyString()))
                .thenReturn(Optional.empty());

        service.saveResults(USER_ID, request(100L));

        verify(quizResultRepository).existsByUserUserIdAndContentIdAndDifficultyAndLangCode(USER_ID, CONTENT_ID, 2, "EN");
        ArgumentCaptor<StoryProgress> saved = ArgumentCaptor.forClass(StoryProgress.class);
        verify(storyProgressRepository).save(saved.capture());
        assertEquals(2, saved.getValue().getLevel());
    }

    /* ---------- fixtures ---------- */

    private static StoryProgress progress(int level) {
        StoryProgress sp = new StoryProgress();
        sp.setUserId(USER_ID);
        sp.setContentId(CONTENT_ID);
        sp.setLanguage("EN");
        sp.setLevel(level);
        sp.setIsCompleted(true);
        return sp;
    }

    private void stubQuiz(long quizId, int difficulty) {
        Content content = new Content();
        content.setContentId(CONTENT_ID);
        content.setTitle("여섯 줄");
        Script script = new Script();
        script.setScriptId(500L);
        script.setContent(content);
        QuizDetail qd = new QuizDetail();
        qd.setQuizId(quizId);
        qd.setContent(content);
        qd.setScript(script);
        qd.setDifficulty(difficulty);
        qd.setQuizType("multiple_choice");
        when(quizDetailRepository.findById(quizId)).thenReturn(Optional.of(qd));
    }

    private static QuestionResultRequestDto request(long quizId) {
        try {
            QuestionResultRequestDto.QuizResultDto r = new QuestionResultRequestDto.QuizResultDto();
            set(r, "quizId", quizId);
            set(r, "attemptCount", 1);
            set(r, "correct", true);
            set(r, "lastAnswer", "bored");
            set(r, "hintUsed", false);
            QuestionResultRequestDto req = new QuestionResultRequestDto();
            set(req, "results", List.of(r));
            return req;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void set(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }
}
