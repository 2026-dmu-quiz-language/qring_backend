package com.qring.qring_backend.domain.quiz;

import com.qring.qring_backend.domain.quiz.ScoreTable;
import com.qring.qring_backend.domain.quiz.ScoreTableId; // 복합키 아이디 클래스
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ScoreTableRepository extends JpaRepository<ScoreTable, ScoreTableId> {

    // JPQL을 이용해 복합키 조건으로 score(점수)만 쏙 뽑아오는 쿼리 메서드
    @Query("SELECT s.score FROM ScoreTable s " +
           "WHERE s.id.difficulty = :difficulty " +
           "AND s.id.attempt = :attempt " +
           "AND s.id.hintUsed = :hintUsed")
    Optional<Integer> findScoreByDifficultyAndAttemptAndHintUsed(
            @Param("difficulty") int difficulty,
            @Param("attempt") int attempt,
            @Param("hintUsed") boolean hintUsed
    );
}