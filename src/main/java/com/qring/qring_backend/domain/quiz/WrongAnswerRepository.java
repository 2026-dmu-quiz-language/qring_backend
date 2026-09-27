package com.qring.qring_backend.domain.quiz;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.qring.qring_backend.dto.quiz.IncorrectResponseDto;

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

    // 오답 목록 — 스토리 + 레벨(difficulty) 단위로 묶고, 가장 최근 오답 시각 포함 (7일 이내, 현재 학습 언어만)
    // 같은 스토리라도 레벨이 다르면 별도 항목으로 나온다.
    @Query("SELECT new com.qring.qring_backend.dto.quiz.IncorrectResponseDto$WrongAnswerItem(" +
           "'STORY', wa.contentId, wa.storyName, qd.difficulty, MAX(wa.createdAt)) " +
           "FROM WrongAnswer wa " +
           "JOIN QuizContent qc ON wa.quizContentId = qc.quizContentId " +
           "JOIN qc.quizDetail qd " +
           "WHERE wa.userId = :userId AND qc.langCode = :langCode " +
           "AND wa.createdAt >= :cutoff " +
           "GROUP BY wa.contentId, wa.storyName, qd.difficulty")
    List<IncorrectResponseDto.WrongAnswerItem> findWrongAnswerSummaryByUserIdAndLangCode(
            @Param("userId") Long userId,
            @Param("langCode") String langCode,
            @Param("cutoff") LocalDateTime cutoff);

    // 재풀이 — contentId(+레벨)로 오답 문제 조회 (7일 이내, 현재 학습 언어만). level 이 null 이면 전체 레벨.
    // quiz_type 보정(options 비었으면 주관식)은 여기서 하지 않는다 —
    // quiz_content.options 는 MySQL json 컬럼이라 JPQL 의 qc.options = '[]' / = '' 비교가 JSON↔문자열 비교가 되어
    // 빈 배열을 걸러내지 못했고, 그 결과 보기 없는 객관식이 그대로 내려갔다.
    // 판정은 IncorrectService 에서 CompetitionQuizTypeUtil.effectiveStoryType 으로 한다 (컴피티션 경로와 공용).
    @Query("SELECT qc FROM WrongAnswer wa " +
           "JOIN QuizContent qc ON wa.quizContentId = qc.quizContentId " +
           "JOIN qc.quizDetail qd " +
           "WHERE wa.userId = :userId AND wa.contentId = :contentId " +
           "AND qc.langCode = :langCode " +
           "AND (:level IS NULL OR qd.difficulty = :level) " +
           "AND wa.createdAt >= :cutoff")
    List<QuizContent> findIncorrectQuizContentsByUserIdAndContentId(
            @Param("userId") Long userId,
            @Param("contentId") Long contentId,
            @Param("langCode") String langCode,
            @Param("level") Integer level,
            @Param("cutoff") LocalDateTime cutoff);

    /** 회원 탈퇴: 사용자의 row 전부 삭제 (UserWithdrawalService 전용, 서비스 트랜잭션 안에서 호출). */
    @Modifying
    @Query("DELETE FROM WrongAnswer wa WHERE wa.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    /**
     * 오답 N일차 푸시 대상 (PUSH_NOTIFICATION_DESIGN.md).
     * [start, end) 에 생성돼 아직 지워지지 않은(다시 풀지 않은) 오답을 사용자별로 센다.
     * 대시보드 오답 알람과 같은 스코프 — 사용자의 현재 학습 언어 오답만. 푸시를 끈 사용자는 제외.
     * (pushEnabled 가 null 인 옛 row 는 MyPageService 와 같이 켜진 것으로 본다.)
     */
    @Query("SELECT wa.userId AS userId, COUNT(wa) AS wrongCount FROM WrongAnswer wa " +
           "JOIN QuizContent qc ON wa.quizContentId = qc.quizContentId " +
           "JOIN User u ON u.userId = wa.userId " +
           "WHERE qc.langCode = u.language " +
           "AND (u.pushEnabled IS NULL OR u.pushEnabled = true) " +
           "AND wa.createdAt >= :start AND wa.createdAt < :end " +
           "GROUP BY wa.userId")
    List<WrongAnswerReminderTarget> findReminderTargetsCreatedBetween(@Param("start") LocalDateTime start,
                                                                      @Param("end") LocalDateTime end);
}