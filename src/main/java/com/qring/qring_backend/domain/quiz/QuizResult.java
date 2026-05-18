package com.qring.qring_backend.domain.quiz;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.qring.qring_backend.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quiz_result")
@Getter
@NoArgsConstructor
public class QuizResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Users 테이블과의 외래키 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; 

    // Content 테이블과의 외래키 연관관계 매핑
    @Column(name = "content_id", nullable = false)
    private Long contentId; 

    @Column(name = "script_id", nullable = false)
    private Long scriptId;

    @Column(nullable = false)
    private int difficulty;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "hint_used", columnDefinition = "BIT(1)")
    private boolean hintUsed;

    @Column(nullable = false)
    private int score;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public QuizResult(User user, Long contentId, Long scriptId, int difficulty, int attemptCount, boolean hintUsed, int score) {
        this.user = user;
        this.contentId = contentId;
        this.scriptId = scriptId;
        this.difficulty = difficulty;
        this.attemptCount = attemptCount;
        this.hintUsed = hintUsed;
        this.score = score;
    }
}
