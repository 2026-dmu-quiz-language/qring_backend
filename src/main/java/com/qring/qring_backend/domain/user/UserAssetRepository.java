package com.qring.qring_backend.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserAssetRepository extends JpaRepository<UserAsset, Long> {

    Optional<UserAsset> findByUserUserId(Long userId);

    @Modifying
    @Query("UPDATE UserAsset ua SET ua.currentPoints = ua.currentPoints + :points WHERE ua.user.userId = :userId")
    void addPoints(@Param("userId") Long userId, @Param("points") int points);

    /** 잔액이 충분할 때만 차감한다 (원자적 — 동시 요청 이중 차감 방지). 반환값 0 이면 잔액 부족. */
    @Modifying
    @Query("UPDATE UserAsset ua SET ua.currentPoints = ua.currentPoints - :points " +
           "WHERE ua.user.userId = :userId AND ua.currentPoints >= :points")
    int tryDeductPoints(@Param("userId") Long userId, @Param("points") int points);

    /** 회원 탈퇴: 사용자의 row 전부 삭제 (UserWithdrawalService 전용, 서비스 트랜잭션 안에서 호출). */
    @Modifying
    @Query("DELETE FROM UserAsset ua WHERE ua.user.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
