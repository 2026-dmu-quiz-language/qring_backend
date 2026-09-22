package com.qring.qring_backend.domain.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAssetHistoryRepository extends JpaRepository<UserAssetHistory, Long> {

    // 유저 포인트 변동 이력 최신순 조회 (마이페이지 등에서 사용 예상)
    @Query("""
        SELECT h FROM UserAssetHistory h
        WHERE h.userId = :userId
        ORDER BY h.createdAt DESC
    """)
    List<UserAssetHistory> findAllByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * 주어진 기간에 해당 사유로 포인트를 받은 적이 있는지.
     * 대시보드가 "오늘 연속 학습 보상을 받았는가"를 판단하는 데 쓴다 — 지급 자체는 학습 시점에 일어나므로
     * 대시보드는 결과만 읽는다.
     */
    @Query("""
        SELECT COUNT(h) > 0 FROM UserAssetHistory h
        WHERE h.userId = :userId
        AND h.sourceType = :sourceType
        AND h.createdAt >= :start
        AND h.createdAt < :end
    """)
    boolean existsByUserIdAndSourceTypeBetween(@Param("userId") Long userId,
                                               @Param("sourceType") UserAssetHistory.SourceType sourceType,
                                               @Param("start") java.time.LocalDateTime start,
                                               @Param("end") java.time.LocalDateTime end);

    /** 회원 탈퇴: 사용자의 row 전부 삭제 (UserWithdrawalService 전용, 서비스 트랜잭션 안에서 호출). */
    @Modifying
    @Query("DELETE FROM UserAssetHistory h WHERE h.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
