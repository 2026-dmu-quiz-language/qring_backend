package com.qring.qring_backend.domain.competition;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionBotProfileRepository extends JpaRepository<CompetitionBotProfile, Integer> {
    // level이 PK라서 findById(level)로 바로 조회 가능
}