package com.qring.qring_backend.domain.quiz;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "story_progress",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "content_id", "language"}))
@Getter
@Setter
@NoArgsConstructor
public class StoryProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "language", nullable = false, length = 10)
    private String language;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}