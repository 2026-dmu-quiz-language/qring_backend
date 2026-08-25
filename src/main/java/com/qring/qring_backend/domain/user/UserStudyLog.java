package com.qring.qring_backend.domain.user;

import java.time.LocalDateTime;

import com.qring.qring_backend.domain.quiz.QuizDetail;

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

/**
 * 사용자 학습 로그 - 사용자·퀴즈별 응답과 정답 여부, 발생 시각 기록.
 * quiz는 스토리 문제 풀이일 때만 채워짐. 봇 컴피티션처럼 스토리 문제와 무관한
 * 활동은 quiz=null로 "이 날짜에 활동했다"는 기록만 남김 (연속 학습일수 계산용).
 */
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
    @JoinColumn(name = "quiz_id", nullable = true)
    private QuizDetail quiz;

    @Column(name = "user_response", columnDefinition = "TEXT")
    private String userResponse;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "lang_code", length = 5)
    private String langCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}