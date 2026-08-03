package com.qring.qring_backend.domain.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
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
}