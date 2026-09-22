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
                    CASE WHEN
                        (SELECT COUNT(qr.id) FROM QuizResult qr
                          WHERE qr.user.userId = :userId AND qr.contentId = c.contentId
                            AND qr.difficulty = :level AND qr.langCode = :language) > 0
                      OR (SELECT COUNT(sp.id) FROM StoryProgress sp
                          WHERE sp.userId = :userId AND sp.contentId = c.contentId
                            AND sp.language = :language AND sp.level = :level AND sp.isCompleted = true) > 0
                    THEN true ELSE false END,
                    CASE
                        WHEN c.requiredPoints IS NULL OR c.requiredPoints = 0 THEN 'UNLOCKED'
                        WHEN COUNT(u.unlockId) > 0 THEN 'UNLOCKED'
                        ELSE 'LOCKED'
                    END,
                    c.requiredPoints
                )
                FROM Content c
                LEFT JOIN QuizDetail q ON q.content.contentId = c.contentId AND q.difficulty = :level
                LEFT JOIN QuizContent qc ON qc.quizDetail.quizId = q.quizId AND qc.langCode = :language
                LEFT JOIN UserContentUnlock u ON u.content.contentId = c.contentId
                    AND u.userId = :userId AND u.language = :language
                GROUP BY c.contentId, c.category.categoryName, c.thumbnailUrl, c.title, c.requiredPoints
            """)
    List<ContentListResponseDto> findContentListByUserId(
            @Param("userId") Long userId,
            @Param("language") String language,
            @Param("level") Integer level);
}