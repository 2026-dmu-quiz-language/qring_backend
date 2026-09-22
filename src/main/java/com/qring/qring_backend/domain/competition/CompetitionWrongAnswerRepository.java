package com.qring.qring_backend.domain.competition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.qring.qring_backend.domain.quiz.WrongAnswerReminderTarget;
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

    // 대시보드 오답 개수: 7일 이내 오답 수. WrongAnswerRepository 의 같은 이름 쿼리(스토리 오답)와 합산해서 쓴다.
    @Query("SELECT COUNT(wa) FROM CompetitionWrongAnswer wa " +
           "WHERE wa.userId = :userId AND wa.langCode = :langCode " +
           "AND wa.createdAt >= :cutoff")
    long countByUserIdAndLangCode(@Param("userId") Long userId,
                                   @Param("langCode") String langCode,
                                   @Param("cutoff") LocalDateTime cutoff);

    // 대시보드 오답 알람: 7일 지난 오답 존재 여부 (WrongAnswerRepository 의 같은 이름 쿼리와 OR 로 합친다)
    @Query("SELECT COUNT(wa) > 0 FROM CompetitionWrongAnswer wa " +
           "WHERE wa.userId = :userId AND wa.langCode = :langCode " +
           "AND wa.createdAt <= :sevenDaysAgo")
    boolean existsByUserIdAndLangCodeAndOlderThan(@Param("userId") Long userId,
                                                   @Param("langCode") String langCode,
                                                   @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);

    /**
     * 오답 N일차 푸시 대상 (컴피티션 오답). 스토리 쪽과 같은 스코프 — 사용자의 현재 학습 언어, 푸시를 끈 사용자 제외.
     * 스토리는 lang_code 를 quiz_content 에서 JOIN 해 오지만 컴피티션 오답은 컬럼으로 들고 있어 바로 비교한다.
     */
    @Query("SELECT wa.userId AS userId, COUNT(wa) AS wrongCount FROM CompetitionWrongAnswer wa " +
           "JOIN User u ON u.userId = wa.userId " +
           "WHERE wa.langCode = u.language " +
           "AND (u.pushEnabled IS NULL OR u.pushEnabled = true) " +
           "AND wa.createdAt >= :start AND wa.createdAt < :end " +
           "GROUP BY wa.userId")
    List<WrongAnswerReminderTarget> findReminderTargetsCreatedBetween(@Param("start") LocalDateTime start,
                                                                       @Param("end") LocalDateTime end);

    @Modifying
    @Query("DELETE FROM CompetitionWrongAnswer wa WHERE wa.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}