package com.qring.qring_backend.domain.content;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StorySessionRepository extends JpaRepository<StorySessionEntity, String> {

    Optional<StorySessionEntity> findBySessionIdAndUserId(String sessionId, Long userId);

    /** 사용자의 가장 최근 진행 중 세션 (앱 재실행 후 이어하기용). */
    Optional<StorySessionEntity> findFirstByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, String status);

    List<StorySessionEntity> findByUserIdAndStatusOrderByArchivedAtDesc(Long userId, String status);

    /** TTL 을 넘긴 미보관(진행 중/미결제) 세션 정리. ARCHIVED 는 건드리지 않는다. */
    @Modifying
    @Transactional
    @Query("DELETE FROM StorySessionEntity s WHERE s.status = :status AND s.updatedAt < :cutoff")
    int deleteExpired(@Param("status") String status, @Param("cutoff") LocalDateTime cutoff);
}
