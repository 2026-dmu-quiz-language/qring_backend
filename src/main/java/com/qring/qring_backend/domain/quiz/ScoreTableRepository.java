package com.qring.qring_backend.domain.quiz;

import com.qring.qring_backend.domain.quiz.ScoreTable;
import com.qring.qring_backend.domain.quiz.ScoreTableId; // 복합키 아이디 클래스
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

/** ScoreTable 조회 — 복합키 조건에 매칭되는 점수만 꺼낸다. */
public interface ScoreTableRepository extends JpaRepository<ScoreTable, ScoreTableId> {

    /** (난이도, 시도 횟수, 힌트 사용 여부) 조합에 해당하는 점수. 정의가 없으면 empty. */
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