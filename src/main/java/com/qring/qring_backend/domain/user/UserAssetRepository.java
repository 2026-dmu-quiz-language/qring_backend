package com.qring.qring_backend.domain.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAssetRepository extends JpaRepository<UserAsset, Long> {
    Optional<UserAsset> findByUserUserId(Long userId);
}
