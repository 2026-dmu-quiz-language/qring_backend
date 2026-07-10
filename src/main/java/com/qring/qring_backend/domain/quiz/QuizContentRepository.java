package com.qring.qring_backend.domain.quiz;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // wrong_answer 저장용: quizId + 유저 언어로 quiz_content_id 찾기
    @Query("SELECT qc FROM QuizContent qc WHERE qc.quizDetail.quizId = :quizId AND qc.langCode = :langCode")
    Optional<QuizContent> findByQuizIdAndLangCode(@Param("quizId") Long quizId, @Param("langCode") String langCode);
}