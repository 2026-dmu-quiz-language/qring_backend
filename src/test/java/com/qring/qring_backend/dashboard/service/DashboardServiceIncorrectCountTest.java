package com.qring.qring_backend.dashboard.service;

import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.dashboard.dto.DashboardResponse;
import com.qring.qring_backend.domain.competition.CompetitionWrongAnswerRepository;
import com.qring.qring_backend.domain.difficulty.DifficultyLevelRepository;
import com.qring.qring_backend.domain.quiz.AchievementCommentRepository;
import com.qring.qring_backend.domain.quiz.QuizResultRepository;
import com.qring.qring_backend.domain.quiz.StoryProgressRepository;
import com.qring.qring_backend.domain.quiz.WrongAnswerRepository;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.domain.user.UserAssetHistoryRepository;
import com.qring.qring_backend.domain.user.UserAssetRepository;
import com.qring.qring_backend.domain.user.UserStudyLogRepository;
import com.qring.qring_backend.domain.user.UserprogressRepository;
import com.qring.qring_backend.service.user.StudyStreakService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 대시보드 오답 개수·알람은 오답 노트와 같은 스코프 — 스토리 오답 + 봇 컴피티션 오답.
 * 회귀 방지 — 오답 노트에 컴피티션 오답을 추가할 때 목록 API 만 고쳐서, 대시보드에는 스토리 오답만 잡혔다.
 */
class DashboardServiceIncorrectCountTest {

    private static final long USER_ID = 19L;
    private static final String LANG = "EN";

    private WrongAnswerRepository wrongAnswerRepository;
    private CompetitionWrongAnswerRepository competitionWrongAnswerRepository;
    private DashboardService service;

    @BeforeEach
    void setUp() {
        UserRepository userRepository = mock(UserRepository.class);
        wrongAnswerRepository = mock(WrongAnswerRepository.class);
        competitionWrongAnswerRepository = mock(CompetitionWrongAnswerRepository.class);
        UserAssetRepository userAssetRepository = mock(UserAssetRepository.class);

        service = new DashboardService(userRepository, mock(UserprogressRepository.class),
                mock(UserStudyLogRepository.class), mock(DifficultyLevelRepository.class),
                mock(AchievementCommentRepository.class), wrongAnswerRepository, competitionWrongAnswerRepository,
                userAssetRepository, mock(UserAssetHistoryRepository.class),
                mock(StoryProgressRepository.class), mock(QuizResultRepository.class),
                mock(StudyStreakService.class));

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(
                User.builder().userId(USER_ID).nickname("Yarr").language(LANG).build()));
        when(userAssetRepository.findCurrentPointsByUserId(USER_ID)).thenReturn(Optional.of(50));
    }

    @Test
    @DisplayName("오답 개수는 스토리 + 봇 컴피티션 합. 같은 문제를 양쪽에서 틀리면 오답 노트에도 따로 뜨므로 중복 제거하지 않는다")
    void incorrectQuizCount_sumsBothSources() {
        when(wrongAnswerRepository.countByUserIdAndLangCode(anyLong(), anyString(), any(LocalDateTime.class)))
                .thenReturn(3L);
        when(competitionWrongAnswerRepository.countByUserIdAndLangCode(anyLong(), anyString(),
                any(LocalDateTime.class))).thenReturn(2L);

        DashboardResponse res = service.getDashboard(USER_ID);

        assertEquals(5, res.getIncorrectQuizCount());
    }

    @Test
    @DisplayName("스토리 오답이 하나도 없어도 봇 컴피티션 오답은 개수에 잡힌다 (이번 버그의 재현 조건)")
    void incorrectQuizCount_countsCompetitionOnly() {
        when(wrongAnswerRepository.countByUserIdAndLangCode(anyLong(), anyString(), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(competitionWrongAnswerRepository.countByUserIdAndLangCode(anyLong(), anyString(),
                any(LocalDateTime.class))).thenReturn(4L);

        assertEquals(4, service.getDashboard(USER_ID).getIncorrectQuizCount());
    }

    @Test
    @DisplayName("7일 지난 오답 알람: 컴피티션 오답만 묵어 있어도 문구를 띄운다")
    void incorrectAlarm_firesOnCompetitionOnly() {
        when(wrongAnswerRepository.existsByUserIdAndLangCodeAndOlderThan(anyLong(), anyString(),
                any(LocalDateTime.class))).thenReturn(false);
        when(competitionWrongAnswerRepository.existsByUserIdAndLangCodeAndOlderThan(anyLong(), anyString(),
                any(LocalDateTime.class))).thenReturn(true);

        assertEquals("오답을 확인한 지 7일이 지났어요! 오답 노트를 확인해 보세요.",
                service.getDashboard(USER_ID).getIncorrectAlarm());
    }

    @Test
    @DisplayName("7일 지난 오답 알람: 양쪽 다 없으면 빈 문자열")
    void incorrectAlarm_emptyWhenNeitherSourceIsOld() {
        when(wrongAnswerRepository.existsByUserIdAndLangCodeAndOlderThan(anyLong(), anyString(),
                any(LocalDateTime.class))).thenReturn(false);
        when(competitionWrongAnswerRepository.existsByUserIdAndLangCodeAndOlderThan(anyLong(), anyString(),
                any(LocalDateTime.class))).thenReturn(false);

        assertEquals("", service.getDashboard(USER_ID).getIncorrectAlarm());
    }
}
