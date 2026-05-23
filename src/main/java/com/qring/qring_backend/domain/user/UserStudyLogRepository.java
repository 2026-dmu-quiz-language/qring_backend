package com.qring.qring_backend.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/** UserStudyLog 조회 (연속 학습일 계산용 날짜 목록 등). */
public interface UserStudyLogRepository extends JpaRepository<UserStudyLog, Long> {

    /** 사용자의 학습일을 중복 제거 + 내림차순으로 반환. 연속 학습일 계산에 사용. */
    @Query(value = """
        SELECT DISTINCT DATE(created_at)
        FROM User_Study_Log
        WHERE user_id = :userId
        ORDER BY DATE(created_at) DESC
        """, nativeQuery = true)
    List<LocalDate> findDistinctStudyDatesDesc(@Param("userId") Long userId);
}
