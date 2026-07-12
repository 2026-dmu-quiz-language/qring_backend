package com.qring.qring_backend.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/** UserStudyLog 조회 (연속 학습일 계산용 날짜 목록 등). */
public interface UserStudyLogRepository extends JpaRepository<UserStudyLog, Long> {

    /** 사용자의 학습일을 중복 제거 + 내림차순으로 반환. 연속 학습일 계산에 사용.*/
    @Query(value = """
        SELECT DISTINCT DATE(created_at)
        FROM user_study_log
        WHERE user_id = :userId AND lang_code = :langCode
        ORDER BY DATE(created_at) DESC
        """, nativeQuery = true)
    List<LocalDate> findDistinctStudyDatesDesc(@Param("userId") Long userId, @Param("langCode") String langCode);

    /** 지정 기간 내 사용자의 학습일(중복 제거)을 반환. 주간 학습 현황 계산에 사용. */
    @Query(value = """
        SELECT DISTINCT DATE(created_at)
        FROM user_study_log
        WHERE user_id = :userId AND lang_code = :langCode
          AND DATE(created_at) BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    List<LocalDate> findStudyDatesBetween(@Param("userId") Long userId,
                                          @Param("langCode") String langCode,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);
}
