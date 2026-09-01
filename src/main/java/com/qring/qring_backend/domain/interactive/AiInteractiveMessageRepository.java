package com.qring.qring_backend.domain.interactive;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiInteractiveMessageRepository extends JpaRepository<AiInteractiveMessage, Long> {

    // 세션 재개 시 지금까지의 대화 전체를 순서대로 불러옴
    @Query("""
        SELECT m FROM AiInteractiveMessage m
        WHERE m.session.sessionId = :sessionId
        ORDER BY m.turnNo ASC
    """)
    List<AiInteractiveMessage> findAllBySessionIdOrderByTurnNo(@Param("sessionId") Long sessionId);

    // 재전송된 메시지인지 확인 (같은 client_message_id로 이미 저장됐는지)
    Optional<AiInteractiveMessage> findBySessionSessionIdAndClientMessageId(Long sessionId, String clientMessageId);

    long countBySessionSessionId(Long sessionId);
}