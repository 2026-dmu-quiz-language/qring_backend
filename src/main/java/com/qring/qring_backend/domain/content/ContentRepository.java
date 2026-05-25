package com.qring.qring_backend.domain.content;

import com.qring.qring_backend.dto.content.ContentListResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {

    @Query("""
        SELECT new com.qring.qring_backend.dto.content.ContentListResponseDto(
            c.category.categoryName,
            c.thumbnailUrl,
            c.title,
            c.contentId,
            COUNT(DISTINCT q.quizId),
            CASE WHEN COUNT(up.progressId) > 0 THEN true ELSE false END,
            c.status
        )
        FROM Content c
        LEFT JOIN QuizDetail q ON q.content.contentId = c.contentId
        LEFT JOIN Userprogress up ON up.content.contentId = c.contentId AND up.user.userId = :userId
        GROUP BY c.contentId, c.category.categoryName, c.thumbnailUrl, c.title, c.status
    """)
    List<ContentListResponseDto> findContentListByUserId(@Param("userId") Long userId);
}
