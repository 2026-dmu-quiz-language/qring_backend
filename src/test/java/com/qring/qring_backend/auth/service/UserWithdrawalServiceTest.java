package com.qring.qring_backend.auth.service;

import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.domain.competition.CompetitionMatchAnswerRepository;
import com.qring.qring_backend.domain.competition.CompetitionMatchRepository;
import com.qring.qring_backend.domain.competition.CompetitionWrongAnswerRepository;
import com.qring.qring_backend.domain.content.StorySessionRepository;
import com.qring.qring_backend.domain.content.UserContentUnlockRepository;
import com.qring.qring_backend.domain.quiz.QuizResultRepository;
import com.qring.qring_backend.domain.quiz.StoryProgressRepository;
import com.qring.qring_backend.domain.quiz.WrongAnswerRepository;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.domain.user.UserAssetHistoryRepository;
import com.qring.qring_backend.domain.user.UserAssetRepository;
import com.qring.qring_backend.domain.user.UserLanguageLevelRepository;
import com.qring.qring_backend.domain.user.UserStudyLogRepository;
import com.qring.qring_backend.domain.user.UserprogressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** 회원 탈퇴: 모든 사용자 데이터 삭제 → users 삭제 → 메모리 상태 정리. 없는 사용자는 아무것도 지우지 않는다. */
class UserWithdrawalServiceTest {

    private static final long USER_ID = 7L;
    private static final String EMAIL = "bye@example.com";

    private UserRepository userRepository;
    private CompetitionMatchAnswerRepository competitionMatchAnswerRepository;
    private CompetitionMatchRepository competitionMatchRepository;
    private CompetitionWrongAnswerRepository competitionWrongAnswerRepository;
    private StorySessionRepository storySessionRepository;
    private UserContentUnlockRepository userContentUnlockRepository;
    private QuizResultRepository quizResultRepository;
    private StoryProgressRepository storyProgressRepository;
    private WrongAnswerRepository wrongAnswerRepository;
    private UserStudyLogRepository userStudyLogRepository;
    private UserprogressRepository userprogressRepository;
    private UserAssetHistoryRepository userAssetHistoryRepository;
    private UserAssetRepository userAssetRepository;
    private UserLanguageLevelRepository userLanguageLevelRepository;
    private EmailService emailService;
    private AuthService authService;
    private UserWithdrawalService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        competitionMatchAnswerRepository = mock(CompetitionMatchAnswerRepository.class);
        competitionMatchRepository = mock(CompetitionMatchRepository.class);
        competitionWrongAnswerRepository = mock(CompetitionWrongAnswerRepository.class);
        storySessionRepository = mock(StorySessionRepository.class);
        userContentUnlockRepository = mock(UserContentUnlockRepository.class);
        quizResultRepository = mock(QuizResultRepository.class);
        storyProgressRepository = mock(StoryProgressRepository.class);
        wrongAnswerRepository = mock(WrongAnswerRepository.class);
        userStudyLogRepository = mock(UserStudyLogRepository.class);
        userprogressRepository = mock(UserprogressRepository.class);
        userAssetHistoryRepository = mock(UserAssetHistoryRepository.class);
        userAssetRepository = mock(UserAssetRepository.class);
        userLanguageLevelRepository = mock(UserLanguageLevelRepository.class);
        emailService = mock(EmailService.class);
        authService = mock(AuthService.class);

        service = new UserWithdrawalService(userRepository,
                competitionMatchAnswerRepository, competitionMatchRepository, competitionWrongAnswerRepository,
                storySessionRepository, userContentUnlockRepository,
                quizResultRepository, storyProgressRepository, wrongAnswerRepository,
                userStudyLogRepository, userprogressRepository,
                userAssetHistoryRepository, userAssetRepository, userLanguageLevelRepository,
                emailService, authService);
    }

    @Test
    @DisplayName("탈퇴 시 자식 테이블 전부 → users 순서로 삭제하고 메모리 코드·토큰을 정리한다")
    void withdraw_deletesEverythingThenUser() {
        User user = User.builder().userId(USER_ID).email(EMAIL).nickname("bye").authProvider("LOCAL").build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        service.withdraw(USER_ID);

        // FK 자식(매치 답안)이 매치보다 먼저, 그리고 모든 자식이 users 보다 먼저 지워져야 한다
        InOrder order = inOrder(competitionMatchAnswerRepository, competitionMatchRepository,
                userAssetHistoryRepository, userAssetRepository, userRepository);
        order.verify(competitionMatchAnswerRepository).deleteAllByMatchUserId(USER_ID);
        order.verify(competitionMatchRepository).deleteAllByUserId(USER_ID);
        order.verify(userAssetHistoryRepository).deleteAllByUserId(USER_ID);
        order.verify(userAssetRepository).deleteAllByUserId(USER_ID);
        order.verify(userRepository).delete(user);

        verify(competitionWrongAnswerRepository).deleteAllByUserId(USER_ID);
        verify(storySessionRepository).deleteAllByUserId(USER_ID);
        verify(userContentUnlockRepository).deleteAllByUserId(USER_ID);
        verify(quizResultRepository).deleteAllByUserId(USER_ID);
        verify(storyProgressRepository).deleteAllByUserId(USER_ID);
        verify(wrongAnswerRepository).deleteAllByUserId(USER_ID);
        verify(userStudyLogRepository).deleteAllByUserId(USER_ID);
        verify(userprogressRepository).deleteAllByUserId(USER_ID);
        verify(userLanguageLevelRepository).deleteAllByUserId(USER_ID);

        verify(emailService).discardCodes(EMAIL);
        verify(authService).discardResetToken(USER_ID);
    }

    @Test
    @DisplayName("없는 사용자면 USER_NOT_FOUND 이고 아무 것도 지우지 않는다")
    void withdraw_unknownUser_deletesNothing() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> service.withdraw(USER_ID));

        assertEquals("USER_NOT_FOUND", e.getMessage());
        verify(userRepository, never()).delete(any());
        verifyNoInteractions(competitionMatchAnswerRepository, competitionMatchRepository,
                competitionWrongAnswerRepository, storySessionRepository, userContentUnlockRepository,
                quizResultRepository, storyProgressRepository, wrongAnswerRepository,
                userStudyLogRepository, userprogressRepository, userAssetHistoryRepository,
                userAssetRepository, userLanguageLevelRepository, emailService, authService);
    }
}
