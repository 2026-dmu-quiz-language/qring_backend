package com.qring.qring_backend.domain.quiz;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.qring.qring_backend.dto.quiz.IncorrectResponseDto;
import com.qring.qring_backend.dto.quiz.IncorrectRetryResponseDto;

public interface WrongAnswerRepository extends JpaRepository<WrongAnswer, Long> {

    Optional<WrongAnswer> findByUserIdAndQuizContentId(Long userId, Long quizContentId);

    void deleteByUserIdAndQuizContentId(Long userId, Long quizContentId);

    // 7일 이내 오답만 조회 (기존 findByUserId 대체)
    @Query("SELECT wa FROM WrongAnswer wa " +
           "WHERE wa.userId = :userId " +
           "AND wa.createdAt >= :cutoff")
    List<WrongAnswer> findByUserId(@Param("userId") Long userId,
                                    @Param("cutoff") LocalDateTime cutoff);

    // 7일 이내 오답 개수만 카운트 (기존 countByUserId 대체)
    @Query("SELECT COUNT(wa) FROM WrongAnswer wa " +
           "WHERE wa.userId = :userId " +
           "AND wa.createdAt >= :cutoff")
    long countByUserId(@Param("userId") Long userId,
                        @Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT COUNT(wa) FROM WrongAnswer wa JOIN QuizContent qc ON wa.quizContentId = qc.quizContentId " +
           "WHERE wa.userId = :userId AND qc.langCode = :langCode " +
           "AND wa.createdAt >= :cutoff")
    long countByUserIdAndLangCode(@Param("userId") Long userId,
                                   @Param("langCode") String langCode,
                                   @Param("cutoff") LocalDateTime cutoff);

    // 대시보드: 특정 언어의 오답 목록 (QuizContent와 JOIN, lang_code로 필터, 7일 이내만)
    @Query("SELECT wa FROM WrongAnswer wa " +
           "JOIN QuizContent qc ON wa.quizContentId = qc.quizContentId " +
           "WHERE wa.userId = :userId AND qc.langCode = :langCode " +
           "AND wa.createdAt >= :cutoff")
    List<WrongAnswer> findByUserIdAndLangCode(@Param("userId") Long userId,
                                               @Param("langCode") String langCode,
                                               @Param("cutoff") LocalDateTime cutoff);

    // 기존 팀원 작성 쿼리 — 7일 지난 오답 존재 여부 체크 (유지)
    @Query("SELECT COUNT(wa) > 0 FROM WrongAnswer wa " +
           "JOIN QuizContent qc ON wa.quizContentId = qc.quizContentId " +
           "WHERE wa.userId = :userId AND qc.langCode = :langCode " +
           "AND wa.createdAt <= :sevenDaysAgo")
    boolean existsByUserIdAndLangCodeAndOlderThan(@Param("userId") Long userId,
                                                  @Param("langCode") String langCode,
                                                  @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);

    // 오답 목록에서 contentId + storyName 중복 없이 조회 (7일 이내만)
    @Query("SELECT DISTINCT new com.qring.qring_backend.dto.quiz.IncorrectResponseDto$WrongAnswerSummary(wa.contentId, wa.storyName) " +
           "FROM WrongAnswer wa " +
           "JOIN QuizContent qc ON wa.quizContentId = qc.quizContentId " +
           "WHERE wa.userId = :userId AND qc.langCode = :langCode " +
           "AND wa.createdAt >= :cutoff")
    List<IncorrectResponseDto.WrongAnswerSummary> findWrongAnswerSummaryByUserIdAndLangCode(
            @Param("userId") Long userId,
            @Param("langCode") String langCode,
            @Param("cutoff") LocalDateTime cutoff);

    // contentId로 오답 문제 목록 조회 (quiz_content JOIN, 7일 이내만)
    @Query("SELECT new com.qring.qring_backend.dto.quiz.IncorrectRetryResponseDto$IncorrectQuizDto(" +
           "qc.quizContentId, qc.question, qc.options, qc.hint, qc.correctAnswer, qc.quizDetail.quizType) " +
           "FROM WrongAnswer wa " +
           "JOIN QuizContent qc ON wa.quizContentId = qc.quizContentId " +
           "WHERE wa.userId = :userId AND wa.contentId = :contentId " +
           "AND wa.createdAt >= :cutoff")
    List<IncorrectRetryResponseDto.IncorrectQuizDto> findIncorrectQuizzesByUserIdAndContentId(
            @Param("userId") Long userId,
            @Param("contentId") Long contentId,
            @Param("cutoff") LocalDateTime cutoff);
}