package com.qring.qring_backend.domain.quiz;

import com.qring.qring_backend.domain.quiz.WrongAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WrongAnswerRepository extends JpaRepository<WrongAnswer, Long> {

    Optional<WrongAnswer> findByUserIdAndQuizContentId(Long userId, Long quizContentId);

    void deleteByUserIdAndQuizContentId(Long userId, Long quizContentId);

    List<WrongAnswer> findByUserId(Long userId);

    // 대시보드: 특정 언어의 오답 목록 (QuizContent와 JOIN, lang_code로 필터)
    @Query("SELECT wa FROM WrongAnswer wa " +
           "JOIN QuizContent qc ON wa.quizContentId = qc.id " +
           "WHERE wa.userId = :userId AND qc.langCode = :langCode")
    List<WrongAnswer> findByUserIdAndLangCode(@Param("userId") Long userId,
                                               @Param("langCode") String langCode);
}
