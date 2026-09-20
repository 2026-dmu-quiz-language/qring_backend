package com.qring.qring_backend.domain.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    // 유저의 특정 콘텐츠에서 정답 횟수 조회
    @Query("""
        SELECT COUNT(r) FROM QuizResult r
        WHERE r.user.userId = :userId
        AND r.contentId = :contentId
        AND r.score > 0
    """)
    int countCorrectByUserIdAndContentId(@Param("userId") Long userId,
                                         @Param("contentId") Long contentId);

    /** 회원 탈퇴: 사용자의 row 전부 삭제 (UserWithdrawalService 전용, 서비스 트랜잭션 안에서 호출). */
    @Modifying
    @Query("DELETE FROM QuizResult r WHERE r.user.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
