package com.qring.qring_backend.domain.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface AchievementCommentRepository extends JpaRepository<AchievementComment, Long> {

    @Query("SELECT ac.commentText FROM AchievementComment ac WHERE :rate BETWEEN ac.minRate AND ac.maxRate")
    Optional<String> findCommentByRate(@Param("rate") int rate);
}