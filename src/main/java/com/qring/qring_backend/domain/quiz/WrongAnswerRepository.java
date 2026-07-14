package com.qring.qring_backend.domain.quiz;

import com.qring.qring_backend.domain.quiz.WrongAnswer;
import com.qring.qring_backend.dto.quiz.IncorrectResponseDto;         // 팀원이 추가한 import
import com.qring.qring_backend.dto.quiz.IncorrectRetryResponseDto;    // 팀원이 추가한 import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WrongAnswerRepository extends JpaRepository<WrongAnswer, Long> {

    Optional<WrongAnswer> findByUserIdAndQuizContentId(Long userId, Long quizContentId);

    void deleteByUserIdAndQuizContentId(Long userId, Long quizContentId);

    List<WrongAnswer> findByUserId(Long userId);
    
    long countByUserId(Long userId);
    
    @Query("SELECT COUNT(wa) FROM WrongAnswer wa JOIN QuizContent qc ON wa.quizContentId = qc.quizContentId WHERE wa.userId = :userId AND qc.langCode = :langCode")
    long countByUserIdAndLangCode(@Param("userId") Long userId, @Param("langCode") String langCode);

    // 대시보드: 특정 언어의 오답 목록 (QuizContent와 JOIN, lang_code로 필터)
    @Query("SELECT wa FROM WrongAnswer wa " +
           "JOIN QuizContent qc ON wa.quizContentId = qc.quizContentId " +
           "WHERE wa.userId = :userId AND qc.langCode = :langCode")
    List<WrongAnswer> findByUserIdAndLangCode(@Param("userId") Long userId,
                                               @Param("langCode") String langCode);


    @Query("SELECT COUNT(wa) > 0 FROM WrongAnswer wa " +
           "JOIN QuizContent qc ON wa.quizContentId = qc.quizContentId " +
           "WHERE wa.userId = :userId AND qc.langCode = :langCode " +
           "AND wa.createdAt <= :sevenDaysAgo")
    boolean existsByUserIdAndLangCodeAndOlderThan(@Param("userId") Long userId,
                                                  @Param("langCode") String langCode,
                                                  @Param("sevenDaysAgo") java.time.LocalDateTime sevenDaysAgo);


    // 오답 목록에서 contentId + storyName 중복 없이 조회
    @Query("SELECT DISTINCT new com.qring.qring_backend.dto.quiz.IncorrectResponseDto$WrongAnswerSummary(wa.contentId, wa.storyName) " +
           "FROM WrongAnswer wa WHERE wa.userId = :userId")
    List<IncorrectResponseDto.WrongAnswerSummary> findWrongAnswerSummaryByUserId(@Param("userId") Long userId);

    // contentId로 오답 문제 목록 조회 (quiz_content JOIN)
    @Query("SELECT new com.qring.qring_backend.dto.quiz.IncorrectRetryResponseDto$IncorrectQuizDto(" +
           "qc.quizContentId, qc.question, qc.options, qc.hint, qc.correctAnswer, qc.quizDetail.quizType) " +
           "FROM WrongAnswer wa " +
           "JOIN QuizContent qc ON wa.quizContentId = qc.quizContentId " +
           "WHERE wa.userId = :userId AND wa.contentId = :contentId")
    List<IncorrectRetryResponseDto.IncorrectQuizDto> findIncorrectQuizzesByUserIdAndContentId(
            @Param("userId") Long userId,
            @Param("contentId") Long contentId);
}