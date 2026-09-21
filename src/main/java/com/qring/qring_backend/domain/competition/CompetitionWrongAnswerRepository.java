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

    @Query("""
        SELECT wa FROM CompetitionWrongAnswer wa
        WHERE wa.userId = :userId
        AND wa.createdAt >= :cutoff
    """)
    List<CompetitionWrongAnswer> findAllByUserId(@Param("userId") Long userId,
                                                  @Param("cutoff") LocalDateTime cutoff);

    Optional<CompetitionWrongAnswer> findByUserIdAndQuizContentIdAndSourceType(
            Long userId, Long quizContentId, CompetitionWrongAnswer.SourceType sourceType);

    void deleteByUserIdAndQuizContentIdAndSourceType(
            Long userId, Long quizContentId, CompetitionWrongAnswer.SourceType sourceType);

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

    // 레벨로 오답 문제 목록 조회 (재풀이용, 7일 이내만) — 원본 문제 조회는 IncorrectService 에서 sourceType 별로 분기
    @Query("""
        SELECT wa FROM CompetitionWrongAnswer wa
        WHERE wa.userId = :userId AND wa.level = :level
        AND wa.createdAt >= :cutoff
    """)
    List<CompetitionWrongAnswer> findByUserIdAndLevel(@Param("userId") Long userId,
                                                       @Param("level") Integer level,
                                                       @Param("cutoff") LocalDateTime cutoff);

    @Modifying
    @Query("DELETE FROM CompetitionWrongAnswer wa WHERE wa.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}