package com.qring.qring_backend.auth.repository;

import com.qring.qring_backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** User 엔티티 CRUD 및 이메일·닉네임·소셜ID 기반 조회 메서드. */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByAuthProviderAndSocialId(String authProvider, String socialId);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
}
