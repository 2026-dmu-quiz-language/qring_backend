package com.qring.qring_backend.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** UserStudyLog 조회 (연속 학습일 계산용 날짜 목록 등). */
public interface UserStudyLogRepository extends JpaRepository<UserStudyLog, Long> {

    /** 사용자의 학습일을 중복 제거 + 내림차순으로 반환. 연속 학습일 계산에 사용.*/
    @Query("SELECT u.createdAt FROM UserStudyLog u WHERE u.user.userId = :userId ORDER BY u.createdAt DESC")
    List<LocalDateTime> findDistinctStudyDatesDesc(@Param("userId") Long userId);

    /** 지정 기간 내 사용자의 학습일(중복 제거)을 반환. 주간 학습 현황 계산에 사용. */
    @Query("SELECT u.createdAt FROM UserStudyLog u WHERE u.user.userId = :userId AND u.createdAt >= :startDate AND u.createdAt < :endDate")
    List<LocalDateTime> findStudyDatesBetween(@Param("userId") Long userId,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * 학습 공백 푸시 대상: 마지막 학습 기록이 [start, end) 에 있는 사용자.
     * 마지막 학습일이 그 하루인 사용자만 걸리므로, 같은 공백 기간에 알림은 한 번만 나간다.
     * 학습 기준은 연속 학습일(StudyStreakService)과 같은 user_study_log 이고 언어는 따지지 않는다.
     * 푸시를 끈 사용자는 제외 (pushEnabled 가 null 인 옛 row 는 켜진 것으로 본다 — 오답 알림과 동일).
     */
    @Query("SELECT u.userId FROM UserStudyLog l JOIN l.user u " +
           "WHERE (u.pushEnabled IS NULL OR u.pushEnabled = true) " +
           "GROUP BY u.userId " +
           "HAVING MAX(l.createdAt) >= :start AND MAX(l.createdAt) < :end")
    List<Long> findUserIdsLastStudiedBetween(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    /** 회원 탈퇴: 사용자의 row 전부 삭제 (UserWithdrawalService 전용, 서비스 트랜잭션 안에서 호출). */
    @Modifying
    @Query("DELETE FROM UserStudyLog u WHERE u.user.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
