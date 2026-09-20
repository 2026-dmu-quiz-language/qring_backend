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

    /**
     * 레벨별 완료 판정의 주 근거. 결과 제출(/questionResult)은 스토리를 끝까지 풀었을 때 한 번만 오므로,
     * (사용자, 콘텐츠, 난이도, 언어) 풀이 기록이 있다 = 그 언어·레벨로 완료했다.
     */
    boolean existsByUserUserIdAndContentIdAndDifficultyAndLangCode(Long userId, Long contentId, int difficulty, String langCode);

    /** 현재 언어·레벨로 완료한 콘텐츠 id 목록 (대시보드 완료 스토리 수). */
    @Query("""
        SELECT DISTINCT r.contentId FROM QuizResult r
        WHERE r.user.userId = :userId
        AND r.langCode = :langCode
        AND r.difficulty = :difficulty
    """)
    java.util.List<Long> findCompletedContentIds(@Param("userId") Long userId,
                                                 @Param("langCode") String langCode,
                                                 @Param("difficulty") int difficulty);

    /** 회원 탈퇴: 사용자의 row 전부 삭제 (UserWithdrawalService 전용, 서비스 트랜잭션 안에서 호출). */
    @Modifying
    @Query("DELETE FROM QuizResult r WHERE r.user.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
