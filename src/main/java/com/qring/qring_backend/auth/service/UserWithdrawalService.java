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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 탈퇴 (하드 삭제). 프론트 안내 문구 "탈퇴 시 모든 정보가 삭제됩니다"에 맞춰
 * 사용자가 남긴 모든 row 를 지운 뒤 users row 를 삭제한다. 전부 한 트랜잭션이라
 * 중간에 실패하면 아무것도 지워지지 않는다.
 *
 * 삭제 순서는 FK 방향을 따른다: competition_match_answer → competition_match,
 * 그 외 테이블은 users 를 직접 참조하므로 users 보다 먼저만 지우면 된다.
 * (quiz_result, user_asset, user_progress, user_study_log 는 DB 에 실제 FK 가 있고
 * 나머지는 user_id 컬럼만 있어 FK 가 없지만, 고아 row 를 남기지 않기 위해 모두 지운다.)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserWithdrawalService {

    private final UserRepository userRepository;
    private final CompetitionMatchAnswerRepository competitionMatchAnswerRepository;
    private final CompetitionMatchRepository competitionMatchRepository;
    private final CompetitionWrongAnswerRepository competitionWrongAnswerRepository;
    private final StorySessionRepository storySessionRepository;
    private final UserContentUnlockRepository userContentUnlockRepository;
    private final QuizResultRepository quizResultRepository;
    private final StoryProgressRepository storyProgressRepository;
    private final WrongAnswerRepository wrongAnswerRepository;
    private final UserStudyLogRepository userStudyLogRepository;
    private final UserprogressRepository userprogressRepository;
    private final UserAssetHistoryRepository userAssetHistoryRepository;
    private final UserAssetRepository userAssetRepository;
    private final UserLanguageLevelRepository userLanguageLevelRepository;
    private final EmailService emailService;
    private final AuthService authService;

    /** 사용자와 사용자가 남긴 모든 데이터를 삭제한다. 없는 사용자면 USER_NOT_FOUND. */
    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));

        // 1) 자식 row 삭제 (FK 자식 → 부모 순서)
        competitionMatchAnswerRepository.deleteAllByMatchUserId(userId);
        competitionMatchRepository.deleteAllByUserId(userId);
        competitionWrongAnswerRepository.deleteAllByUserId(userId);
        storySessionRepository.deleteAllByUserId(userId);
        userContentUnlockRepository.deleteAllByUserId(userId);
        quizResultRepository.deleteAllByUserId(userId);
        storyProgressRepository.deleteAllByUserId(userId);
        wrongAnswerRepository.deleteAllByUserId(userId);
        userStudyLogRepository.deleteAllByUserId(userId);
        userprogressRepository.deleteAllByUserId(userId);
        userAssetHistoryRepository.deleteAllByUserId(userId);
        userAssetRepository.deleteAllByUserId(userId);
        userLanguageLevelRepository.deleteAllByUserId(userId);

        // 2) 계정 삭제 (이메일·닉네임 유니크가 풀리므로 같은 이메일로 재가입 가능)
        userRepository.delete(user);

        // 3) 서버 메모리에 남은 인증 코드·재설정 토큰 정리
        emailService.discardCodes(user.getEmail());
        authService.discardResetToken(userId);

        log.info("[WITHDRAW] userId={} provider={} 탈퇴 완료", userId, user.getAuthProvider());
    }
}
