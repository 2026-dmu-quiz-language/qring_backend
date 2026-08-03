package com.qring.qring_backend.domain.competition;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompetitionMatchAnswerRepository extends JpaRepository<CompetitionMatchAnswer, Long> {

    // 매치 내 라운드 순서대로 전체 답안 조회 (결과 화면, 진행률 계산용)
    @Query("""
        SELECT a FROM CompetitionMatchAnswer a
        WHERE a.match.matchId = :matchId
        ORDER BY a.roundNo ASC
    """)
    List<CompetitionMatchAnswer> findAllByMatchIdOrderByRoundNo(@Param("matchId") Long matchId);

    // 다음 라운드 번호 계산용
    long countByMatchMatchId(Long matchId);
}