package com.qring.qring_backend.domain.user;

import com.qring.qring_backend.domain.quiz.QuizDetail;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/** 사용자 퀴즈 풀이 로그 — 사용자·퀴즈별 응답과 정답 여부, 발생 시각 기록. */
@Entity
@Table(name = "User_Study_Log")
@Getter @Setter @NoArgsConstructor
public class UserStudyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private QuizDetail quiz;

    @Column(name = "user_response", columnDefinition = "TEXT")
    private String userResponse;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}