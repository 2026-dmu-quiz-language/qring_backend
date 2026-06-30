package com.qring.qring_backend.domain.quiz;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wrong_answer",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "quiz_content_id"}))
@Getter
@Setter
@NoArgsConstructor
public class WrongAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "quiz_content_id", nullable = false)
    private Long quizContentId;

    @Column(name = "level", nullable = false)
    private Integer level;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}