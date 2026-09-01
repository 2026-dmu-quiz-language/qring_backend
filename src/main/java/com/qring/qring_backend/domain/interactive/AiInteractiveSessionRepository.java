package com.qring.qring_backend.domain.interactive;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.qring.qring_backend.domain.interactive.AiInteractiveSession.Status;

public interface AiInteractiveSessionRepository extends JpaRepository<AiInteractiveSession, Long> {

    // 재연결 시 이어갈 세션 찾기 (유저당 진행 중인 세션은 1개라고 가정)
    @Query("""
        SELECT s FROM AiInteractiveSession s
        WHERE s.userId = :userId
        AND s.status = :status
    """)
    List<AiInteractiveSession> findAllByUserIdAndStatus(@Param("userId") Long userId,
                                                        @Param("status") Status status);

    Optional<AiInteractiveSession> findBySessionIdAndUserId(Long sessionId, Long userId);
}