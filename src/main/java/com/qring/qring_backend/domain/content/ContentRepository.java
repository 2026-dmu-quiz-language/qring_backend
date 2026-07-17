package com.qring.qring_backend.domain.content;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.qring.qring_backend.dto.content.ContentListResponseDto;

public interface ContentRepository extends JpaRepository<Content, Long> {

    @Query("""
        SELECT new com.qring.qring_backend.dto.content.ContentListResponseDto(
            c.contentId,
            c.category.categoryName,
            c.thumbnailUrl,
            c.title,
            COUNT(DISTINCT qc.quizContentId),
            CASE WHEN COUNT(sp.id) > 0 THEN true ELSE false END,
            c.status,
            c.requiredPoints
        )
        FROM Content c
        LEFT JOIN QuizDetail q ON q.content.contentId = c.contentId AND q.difficulty = :level
        LEFT JOIN QuizContent qc ON qc.quizDetail.quizId = q.quizId AND qc.langCode = :language
        LEFT JOIN StoryProgress sp ON sp.contentId = c.contentId AND sp.userId = :userId AND sp.language = :language AND sp.isCompleted = true
        GROUP BY c.contentId, c.category.categoryName, c.thumbnailUrl, c.title, c.status, c.requiredPoints
    """)
    List<ContentListResponseDto> findContentListByUserId(
            @Param("userId") Long userId,
            @Param("language") String language,
            @Param("level") Integer level);
}