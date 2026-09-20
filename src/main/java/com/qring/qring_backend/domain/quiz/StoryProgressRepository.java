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

    // 대시보드: 언어별 완료 스토리 수
    int countByUserIdAndLanguageAndIsCompleted(Long userId, String language, Boolean isCompleted);

    // 콘텐츠 목록: 유저의 언어별 완료 스토리 목록
    List<StoryProgress> findByUserIdAndLanguage(Long userId, String language);

    /** 회원 탈퇴: 사용자의 row 전부 삭제 (UserWithdrawalService 전용, 서비스 트랜잭션 안에서 호출). */
    @Modifying
    @Query("DELETE FROM StoryProgress p WHERE p.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
