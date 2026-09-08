package com.qring.qring_backend.domain.interactive;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.qring.qring_backend.domain.interactive.StorySession.Status;

public interface StorySessionRepository extends JpaRepository<StorySession, String> {

    // 유저별 보관 목록 조회 (인덱스: user_id, status)
    List<StorySession> findAllByUserIdAndStatus(Long userId, Status status);

    Optional<StorySession> findBySessionIdAndUserId(String sessionId, Long userId);

    // TTL 정리용: 오래 방치된 진행 중 세션 조회 (예: 배치로 자동 삭제할 때)
    @Query("""
        SELECT s FROM StorySession s
        WHERE s.status = 'IN_PROGRESS'
        AND s.updatedAt < :cutoff
    """)
    List<StorySession> findStaleInProgressSessions(@Param("cutoff") java.time.LocalDateTime cutoff);
}