package com.qring.qring_backend.domain.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizContentRepository extends JpaRepository<QuizContent, Long> {

    @Query("""
        SELECT qc FROM QuizContent qc
        JOIN qc.quizDetail q
        WHERE q.content.contentId = :contentId
        AND q.difficulty = :difficulty
        AND qc.langCode = :langCode
    """)
    List<QuizContent> findAllByContentIdAndDifficultyAndLanguage(
            @Param("contentId") Long contentId,
            @Param("difficulty") Integer difficulty,
            @Param("langCode") String langCode);
}