package com.qring.qring_backend.auth.repository;

import com.qring.qring_backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByAuthProviderAndSocialId(String authProvider, String socialId);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
}
