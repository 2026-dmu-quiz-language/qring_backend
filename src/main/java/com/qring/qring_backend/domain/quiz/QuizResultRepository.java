package com.qring.qring_backend.domain.quiz;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    // 💡 특정 유저의 누적 총점을 실시간으로 더해서 가져오는 JPQL 쿼리
    @Query("SELECT SUM(q.score) FROM QuizResult q WHERE q.user.id = :userId")
    Optional<Long> javaSumScoreByUserId(@Param("userId") Long userId);
}