package com.qring.qring_backend.domain.quiz;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** QuizResult 조회: 사용자별 누적 점수 등. */
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    /** 특정 유저의 누적 총점 (퀴즈 풀이 이력이 없으면 empty). */
    @Query("SELECT SUM(q.score) FROM QuizResult q WHERE q.user.id = :userId")
    Optional<Long> javaSumScoreByUserId(@Param("userId") Long userId);
}