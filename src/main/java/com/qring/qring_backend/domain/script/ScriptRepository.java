package com.qring.qring_backend.domain.script;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScriptRepository extends JpaRepository<Script, Long> {

    @Query("""
        SELECT s FROM Script s
        JOIN s.chapter c
        WHERE c.content.contentId = :contentId
        ORDER BY c.chapterNum ASC, s.sequenceNum ASC
    """)
    List<Script> findAllByContentId(@Param("contentId") Long contentId);
}
