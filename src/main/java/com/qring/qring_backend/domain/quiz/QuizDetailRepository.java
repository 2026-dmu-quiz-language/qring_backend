package com.qring.qring_backend.domain.quiz;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
        SELECT q FROM QuizDetail q
        WHERE q.content.contentId = :contentId
        AND q.difficulty = :difficulty
    """)
    List<QuizDetail> findAllByContentIdAndDifficulty(@Param("contentId") Long contentId,
                                                     @Param("difficulty") Integer difficulty);

    // 봇 컴피티션 - 스토리 문제 풀링용 (콘텐츠 무관, 난이도만으로 전체 조회)
    @Query("""
        SELECT q FROM QuizDetail q
        WHERE q.difficulty = :difficulty
    """)
    List<QuizDetail> findAllByDifficulty(@Param("difficulty") Integer difficulty);
}