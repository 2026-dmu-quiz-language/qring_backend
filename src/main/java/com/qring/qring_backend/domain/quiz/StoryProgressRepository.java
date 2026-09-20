package com.qring.qring_backend.domain.quiz;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

public interface StoryProgressRepository extends JpaRepository<StoryProgress, Long> {

    Optional<StoryProgress> findByUserIdAndContentIdAndLanguage(
            Long userId, Long contentId, String language);

    /**
     * 마지막 완료 레벨이 :level 인 콘텐츠 id — 레벨별 완료 판정의 보조 근거.
     * (주 근거는 quiz_result. 옛 quiz_result 에 lang_code 가 없는 경우를 이 테이블이 메운다.)
     */
    @Query("SELECT sp.contentId FROM StoryProgress sp " +
           "WHERE sp.userId = :userId AND sp.language = :language AND sp.level = :level AND sp.isCompleted = true")
    List<Long> findCompletedContentIds(@Param("userId") Long userId,
                                       @Param("language") String language,
                                       @Param("level") Integer level);

    /** 회원 탈퇴: 사용자의 row 전부 삭제 (UserWithdrawalService 전용, 서비스 트랜잭션 안에서 호출). */
    @Modifying
    @Query("DELETE FROM StoryProgress p WHERE p.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
