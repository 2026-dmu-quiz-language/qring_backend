package com.qring.qring_backend.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
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
}
