package com.qring.qring_backend.domain.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

/** AchievementComment 조회 — 입력 진도율이 속한 구간의 코멘트 문구. */
public interface AchievementCommentRepository extends JpaRepository<AchievementComment, Long> {

    /** rate 가 [minRate, maxRate] 구간에 포함되는 코멘트 텍스트 반환. */
    @Query("SELECT ac.commentText FROM AchievementComment ac WHERE :rate BETWEEN ac.minRate AND ac.maxRate")
    Optional<String> findCommentByRate(@Param("rate") int rate);
}