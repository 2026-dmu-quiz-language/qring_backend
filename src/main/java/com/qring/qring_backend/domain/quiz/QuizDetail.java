package com.qring.qring_backend.domain.quiz;

import com.qring.qring_backend.domain.content.Content;
import com.qring.qring_backend.domain.script.Script;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 스크립트에 연결된 퀴즈 — 유형, 정답, 해설 보관. */
@Entity
@Table(name = "Quiz_Detail")
@Getter @Setter @NoArgsConstructor
public class QuizDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id")
    private Long quizId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "script_id", nullable = false)
    private Script script;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Column(name = "quiz_type", length = 200)
    private String quizType;

    @Column(name = "difficulty")
    private Integer difficulty;
}