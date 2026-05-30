package com.qring.qring_backend.domain.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizDetailRepository extends JpaRepository<QuizDetail, Long> {

    @Query("""
        SELECT q FROM QuizDetail q
        WHERE q.content.contentId = :contentId
    """)
    List<QuizDetail> findAllByContentId(@Param("contentId") Long contentId);

    @Query("""
        SELECT COUNT(q) FROM QuizDetail q
        WHERE q.content.contentId = :contentId
    """)
    Long countByContentId(@Param("contentId") Long contentId);
}