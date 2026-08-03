package com.qring.qring_backend.domain.competition;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 봇 컴피티션 문제 — 언어별 콘텐츠(질문, 정답, 타입별 전용 필드). */
@Entity
@Table(
        name = "competition_quiz_content",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_quiz_lang",
                columnNames = {"quiz_id", "lang_code"}
        )
)
@Getter @Setter @NoArgsConstructor
public class CompetitionQuizContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_content_id")
    private Long quizContentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private CompetitionQuizDetail quizDetail;

    @Column(name = "lang_code", length = 5, nullable = false)
    private String langCode;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;

    /** word_arrange 전용: 한글 문장 */
    @Column(columnDefinition = "TEXT")
    private String korean;

    /** word_arrange 전용: 배열용 단어 타일 */
    @Column(columnDefinition = "JSON")
    private String tiles;

    /** word_arrange 전용: 정답 타일 순서 */
    @Column(name = "answer_tiles", columnDefinition = "JSON")
    private String answerTiles;

    /** word_arrange 전용: 오답 유도용 타일 */
    @Column(name = "distractor_tiles", columnDefinition = "JSON")
    private String distractorTiles;

    /** multiple_choice 전용: 보기 목록 */
    @Column(columnDefinition = "JSON")
    private String options;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String answer;

    /** subjective 위주로 사용: 정답으로 인정할 답변 목록 */
    @Column(name = "acceptable_answers", columnDefinition = "JSON")
    private String acceptableAnswers;
}