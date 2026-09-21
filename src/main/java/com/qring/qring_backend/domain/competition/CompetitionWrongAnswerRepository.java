package com.qring.qring_backend.domain.competition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.qring.qring_backend.dto.quiz.IncorrectResponseDto;

public interface CompetitionWrongAnswerRepository extends JpaRepository<CompetitionWrongAnswer, Long> {

    // 유저의 컴피티션 오답 전체 조회 (7일 이내만, 스토리 오답과 합쳐서 보여줄 때 사용)
    @Query("""
        SELECT wa FROM CompetitionWrongAnswer wa
        WHERE wa.userId = :userId
        AND wa.createdAt >= :cutoff
    """)
    List<CompetitionWrongAnswer> findAllByUserId(@Param("userId") Long userId,
                                                  @Param("cutoff") LocalDateTime cutoff);

    @Query("""
        SELECT wa FROM CompetitionWrongAnswer wa
        WHERE wa.userId = :userId
        AND wa.quizContent.quizContentId = :quizContentId
    """)
    Optional<CompetitionWrongAnswer> findByUserIdAndQuizContentId(@Param("userId") Long userId,
                                                                  @Param("quizContentId") Long quizContentId);

    // 정답 처리 시 오답 목록에서 제거 (기존 패턴과 동일)
    void deleteByUserIdAndQuizContentQuizContentId(Long userId, Long quizContentId);

    // 레벨별 중복 없이, 가장 최근 오답 시각 포함 조회 (7일 이내만) — STORY/COMPETITION 통합 목록 정렬용
    @Query("SELECT new com.qring.qring_backend.dto.quiz.IncorrectResponseDto$WrongAnswerItem(" +
           "'COMPETITION', CAST(wa.level AS long), CONCAT('레벨 ', wa.level, ' 컴피티션'), MAX(wa.createdAt)) " +
           "FROM CompetitionWrongAnswer wa " +
           "WHERE wa.userId = :userId AND wa.langCode = :langCode " +
           "AND wa.createdAt >= :cutoff " +
           "GROUP BY wa.level")
    List<IncorrectResponseDto.WrongAnswerItem> findWrongAnswerSummaryByUserIdAndLangCode(
            @Param("userId") Long userId,
            @Param("langCode") String langCode,
            @Param("cutoff") LocalDateTime cutoff);

    // 레벨로 오답 문제 목록 조회 (재풀이용, 7일 이내만) — quizContent를 fetch join해서 N+1 방지
    @Query("""
        SELECT wa FROM CompetitionWrongAnswer wa
        JOIN FETCH wa.quizContent qc
        WHERE wa.userId = :userId AND wa.level = :level
        AND wa.createdAt >= :cutoff
    """)
    List<CompetitionWrongAnswer> findByUserIdAndLevel(@Param("userId") Long userId,
                                                       @Param("level") Integer level,
                                                       @Param("cutoff") LocalDateTime cutoff);

    /** 회원 탈퇴: 사용자의 row 전부 삭제 (UserWithdrawalService 전용, 서비스 트랜잭션 안에서 호출). */
    @Modifying
    @Query("DELETE FROM CompetitionWrongAnswer wa WHERE wa.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}