package com.qring.qring_backend.domain.user;


import java.util.List;
import java.util.Optional;

import com.qring.qring_backend.domain.user.UserLanguageLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserLanguageLevelRepository extends JpaRepository<UserLanguageLevel, Long> {

    Optional<UserLanguageLevel> findByUserIdAndLanguage(Long userId, String language);

    List<UserLanguageLevel> findByUserId(Long userId);

    boolean existsByUserIdAndLanguage(Long userId, String language);

    // 첫 풀이 보너스 원자적 지급: affected row가 1이면 이번이 처음 지급된 것
    @Modifying
    @Query("UPDATE UserLanguageLevel u SET u.bonusAwarded = true " +
           "WHERE u.userId = :userId AND u.language = :language AND u.bonusAwarded = false")
    int awardBonusIfFirstTime(@Param("userId") Long userId, @Param("language") String language);
}
