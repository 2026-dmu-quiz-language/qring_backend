package com.qring.qring_backend.domain.content;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserContentUnlockRepository extends JpaRepository<UserContentUnlock, Long> {

    Optional<UserContentUnlock> findByUserIdAndContentContentId(Long userId, Long contentId);

    boolean existsByUserIdAndContentContentId(Long userId, Long contentId);
}