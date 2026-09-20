package com.qring.qring_backend.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserAssetRepository extends JpaRepository<UserAsset, Long> {

    Optional<UserAsset> findByUserUserId(Long userId);

    /**
     * 현재 잔액을 스칼라로 읽는다. 엔티티 조회와 달리 영속성 컨텍스트에 남은 옛 값을 돌려주지 않으므로
     * 벌크 UPDATE(addPoints/tryDeductPoints) 직후 잔액 확인에 쓴다. 빈 Optional 은 자산 row 없음.
     */
    @Query("SELECT COALESCE(ua.currentPoints, 0) FROM UserAsset ua WHERE ua.user.userId = :userId")
    Optional<Integer> findCurrentPointsByUserId(@Param("userId") Long userId);

    /** 마지막으로 15일 연속 보상을 지급한 연속일 (중복 지급 가드값). 빈 Optional 은 자산 row 없음. */
    @Query("SELECT COALESCE(ua.streakDays, 0) FROM UserAsset ua WHERE ua.user.userId = :userId")
    Optional<Integer> findStreakDaysByUserId(@Param("userId") Long userId);

    /** 보상 가드값 갱신. 엔티티 save 대신 벌크 UPDATE 로 해야 같은 트랜잭션의 포인트 변경을 덮어쓰지 않는다. */
    @Modifying
    @Query("UPDATE UserAsset ua SET ua.streakDays = :streakDays WHERE ua.user.userId = :userId")
    int updateStreakDays(@Param("userId") Long userId, @Param("streakDays") int streakDays);

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
