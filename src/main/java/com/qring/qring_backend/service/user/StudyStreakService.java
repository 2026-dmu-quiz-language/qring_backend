package com.qring.qring_backend.service.user;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qring.qring_backend.domain.user.UserAssetHistory.SourceType;
import com.qring.qring_backend.domain.user.UserAssetRepository;
import com.qring.qring_backend.domain.user.UserStudyLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 연속 학습일 계산과 주기 보상 지급.
 *
 * 지급 시점은 학습 기록이 쌓이는 순간이다 (스토리 퀴즈 완료, 봇 컴피티션 완료).
 * 예전에는 대시보드 조회 시점에 "연속일이 15의 배수일 때만" 지급해서, 15일째에 대시보드를 열지 않고
 * 16일째에 열면 그 보상을 영영 받지 못했다 (2026-09-23 수정).
 *
 * user_asset.streak_days 는 "마지막으로 보상한 연속일" 가드값이다. 연속이 끊겨 다시 세기 시작하면
 * 현재 연속일이 가드값보다 작아지는데, 그때 가드를 0 으로 되돌리지 않으면 두 번째 15일이 조건을 통과하지
 * 못해 재달성 보상이 나가지 않았다 (테스트 케이스 #173).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudyStreakService {

    /** 보상 주기(일)와 금액. 15·30·45… 일째에 지급. */
    public static final int REWARD_INTERVAL_DAYS = 15;
    public static final int REWARD_POINTS = 30;

    private final UserStudyLogRepository userStudyLogRepository;
    private final UserAssetRepository userAssetRepository;
    private final UserPointService userPointService;

    /**
     * 오늘까지 이어진 연속 학습일. 가장 최근 학습일이 오늘 또는 어제여야 세기 시작하고,
     * 하루라도 비면 거기서 끊는다. 그 이전에 끊겼으면 0.
     */
    @Transactional(readOnly = true)
    public long currentStreak(Long userId) {
        List<LocalDate> dates = userStudyLogRepository.findDistinctStudyDatesDesc(userId).stream()
                .map(LocalDateTime::toLocalDate)
                .distinct()
                .toList();

        if (dates.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        LocalDate latest = dates.get(0);
        if (!latest.equals(today) && !latest.equals(today.minusDays(1))) {
            return 0;
        }

        long count = 1;
        for (int i = 1; i < dates.size(); i++) {
            if (!dates.get(i).equals(dates.get(i - 1).minusDays(1))) {
                break;
            }
            count++;
        }
        return count;
    }

    /**
     * 학습 기록을 남긴 직후 호출 — 연속일이 보상 주기에 도달했으면 그 자리에서 지급한다.
     * 호출부의 트랜잭션에 참여하므로 포인트·이력·가드값이 학습 기록과 함께 커밋된다.
     *
     * @return 이번 호출에서 지급했으면 true
     */
    @Transactional
    public boolean awardIfDue(Long userId) {
        Optional<Integer> guard = userAssetRepository.findStreakDaysByUserId(userId);
        if (guard.isEmpty()) {
            // 자산 row 가 없는 사용자 (가입 절차가 끝나지 않은 경우). 학습 저장을 막지 않도록 조용히 건너뛴다.
            return false;
        }

        long streak = currentStreak(userId);
        if (streak <= 0) {
            return false;
        }

        int lastRewarded = guard.get();
        if (streak < lastRewarded) {
            // 연속이 끊겨 새로 세는 중 — 가드를 되돌려야 다음 주기 보상이 나간다
            log.info("[Streak] userId={} 연속 끊김 감지 (현재 {}일 < 마지막 지급 {}일) — 가드 리셋",
                    userId, streak, lastRewarded);
            userAssetRepository.updateStreakDays(userId, 0);
            lastRewarded = 0;
        }

        if (streak % REWARD_INTERVAL_DAYS != 0 || streak <= lastRewarded) {
            return false;
        }

        // 가드를 먼저 선점한다 — 같은 날 두 번 학습하거나 요청이 겹쳐도 한 번만 지급된다
        if (userAssetRepository.updateStreakDaysIfHigher(userId, (int) streak) == 0) {
            return false;
        }

        userPointService.earn(userId, REWARD_POINTS, SourceType.STREAK_REWARD, streak);
        log.info("[Streak] userId={} 연속 {}일 보상 +{} pt 지급", userId, streak, REWARD_POINTS);
        return true;
    }
}
