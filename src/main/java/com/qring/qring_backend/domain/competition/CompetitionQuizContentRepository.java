package com.qring.qring_backend.domain.competition;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompetitionQuizContentRepository extends JpaRepository<CompetitionQuizContent, Long> {

    // 특정 레벨 + 언어의 모든 문제 조회 (매치 시작 시 문제 세트 로딩용)
    @Query("""
        SELECT qc FROM CompetitionQuizContent qc
        JOIN qc.quizDetail q
        WHERE q.level = :level
        AND qc.langCode = :langCode
    """)
    List<CompetitionQuizContent> findAllByLevelAndLangCode(@Param("level") Integer level,
                                                           @Param("langCode") String langCode);

    // 오답 저장용: quizId + 언어로 quiz_content_id 찾기 (기존 QuizContentRepository 패턴과 동일)
    @Query("""
        SELECT qc FROM CompetitionQuizContent qc
        WHERE qc.quizDetail.quizId = :quizId
        AND qc.langCode = :langCode
    """)
    Optional<CompetitionQuizContent> findByQuizIdAndLangCode(@Param("quizId") Long quizId,
                                                             @Param("langCode") String langCode);
}