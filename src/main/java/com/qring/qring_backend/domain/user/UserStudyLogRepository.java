package com.qring.qring_backend.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface UserStudyLogRepository extends JpaRepository<UserStudyLog, Long> {

    @Query(value = """
        SELECT DISTINCT DATE(created_at)
        FROM User_Study_Log
        WHERE user_id = :userId
        ORDER BY DATE(created_at) DESC
        """, nativeQuery = true)
    List<LocalDate> findDistinctStudyDatesDesc(@Param("userId") Long userId);
}
