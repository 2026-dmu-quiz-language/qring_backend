package com.qring.qring_backend.domain.competition;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.qring.qring_backend.domain.competition.CompetitionMatch.MatchStatus;

public interface CompetitionMatchRepository extends JpaRepository<CompetitionMatch, Long> {

    // 유저가 진행 중/일시정지 중인 매치가 있는지 확인 (중복 입장 방지, 재개용)
    @Query("""
        SELECT m FROM CompetitionMatch m
        WHERE m.userId = :userId
        AND m.status IN :statuses
    """)
    List<CompetitionMatch> findAllByUserIdAndStatusIn(@Param("userId") Long userId,
                                                      @Param("statuses") List<MatchStatus> statuses);

    Optional<CompetitionMatch> findByMatchIdAndUserId(Long matchId, Long userId);
}