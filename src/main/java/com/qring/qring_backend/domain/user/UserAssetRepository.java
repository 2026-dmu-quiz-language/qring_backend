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
}