package com.qring.qring_backend.domain.quiz;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "quiz_content")
@Getter @Setter @NoArgsConstructor
public class QuizContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_content_id")
    private Long quizContentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private QuizDetail quizDetail;

    @Column(name = "lang_code", length = 5, nullable = false)
    private String langCode;

    @Column(columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "JSON")
    private String options;

    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(columnDefinition = "TEXT")
    private String hint;

    @Column(name = "acceptable_answers", columnDefinition = "JSON")
    private String acceptableAnswers;
}