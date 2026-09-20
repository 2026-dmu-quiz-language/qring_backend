package com.qring.qring_backend.domain.content;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

public interface UserContentUnlockRepository extends JpaRepository<UserContentUnlock, Long> {

    Optional<UserContentUnlock> findByUserIdAndContentContentId(Long userId, Long contentId);

    boolean existsByUserIdAndContentContentId(Long userId, Long contentId);

    /** 회원 탈퇴: 사용자의 row 전부 삭제 (UserWithdrawalService 전용, 서비스 트랜잭션 안에서 호출). */
    @Modifying
    @Query("DELETE FROM UserContentUnlock u WHERE u.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
