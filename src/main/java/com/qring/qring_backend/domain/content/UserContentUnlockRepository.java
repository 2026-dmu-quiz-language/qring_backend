package com.qring.qring_backend.domain.content;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserContentUnlockRepository extends JpaRepository<UserContentUnlock, Long> {

    Optional<UserContentUnlock> findByUserIdAndContentContentIdAndLanguage(
            Long userId, Long contentId, String language);

    boolean existsByUserIdAndContentContentIdAndLanguage(
            Long userId, Long contentId, String language);

    // 회원 탈퇴: 사용자의 해금 기록 전부 삭제 (언어 무관, UserWithdrawalService 전용)
    @Modifying
    @Query("DELETE FROM UserContentUnlock u WHERE u.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}