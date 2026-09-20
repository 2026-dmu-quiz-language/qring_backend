package com.qring.qring_backend.domain.competition;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompetitionQuizDetailRepository extends JpaRepository<CompetitionQuizDetail, Long> {

    // 세트 안에서 (level + origin_id) 로 기존 문제 존재 여부 확인 (다른 언어 파일이 같은 row 에 붙는 키)
    @Query("""
        SELECT q FROM CompetitionQuizDetail q
        WHERE q.setKey = :setKey
        AND q.level = :level
        AND q.originId = :originId
    """)
    Optional<CompetitionQuizDetail> findBySetKeyAndLevelAndOriginId(@Param("setKey") String setKey,
                                                                    @Param("level") Integer level,
                                                                    @Param("originId") Integer originId);
}
