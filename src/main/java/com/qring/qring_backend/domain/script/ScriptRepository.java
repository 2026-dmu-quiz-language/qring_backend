package com.qring.qring_backend.domain.script;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScriptRepository extends JpaRepository<Script, Long> {

    @Query("""
        SELECT s FROM Script s
        WHERE s.content.contentId = :contentId
        ORDER BY s.sequenceNum ASC
    """)
    List<Script> findAllByContentId(@Param("contentId") Long contentId);
}