package com.qring.qring_backend.domain.competition;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompetitionQuizDetailRepository extends JpaRepository<CompetitionQuizDetail, Long> {

    // 언어 간 문제 매칭 키 (level + origin_id) 로 기존 문제 존재 여부 확인
    @Query("""
        SELECT q FROM CompetitionQuizDetail q
        WHERE q.level = :level
        AND q.originId = :originId
    """)
    Optional<CompetitionQuizDetail> findByLevelAndOriginId(@Param("level") Integer level,
                                                           @Param("originId") Integer originId);
}