package com.qring.qring_backend.domain.competition;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompetitionWrongAnswerRepository extends JpaRepository<CompetitionWrongAnswer, Long> {

    // 유저의 컴피티션 오답 전체 조회 (오답 기능에서 스토리 오답과 합쳐서 보여줄 때 사용)
    @Query("""
        SELECT wa FROM CompetitionWrongAnswer wa
        WHERE wa.userId = :userId
    """)
    List<CompetitionWrongAnswer> findAllByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT wa FROM CompetitionWrongAnswer wa
        WHERE wa.userId = :userId
        AND wa.quizContent.quizContentId = :quizContentId
    """)
    Optional<CompetitionWrongAnswer> findByUserIdAndQuizContentId(@Param("userId") Long userId,
                                                                  @Param("quizContentId") Long quizContentId);

    // 정답 처리 시 오답 목록에서 제거 (기존 WrongAnswerRepository 패턴과 동일)
    void deleteByUserIdAndQuizContentQuizContentId(Long userId, Long quizContentId);
}