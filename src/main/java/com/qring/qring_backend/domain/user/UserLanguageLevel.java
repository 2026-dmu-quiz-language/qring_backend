package com.qring.qring_backend.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_language_level",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "language"}))
@Getter
@Setter
@NoArgsConstructor
public class UserLanguageLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "language", nullable = false, length = 10)
    private String language;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "bonus_awarded", nullable = false)
    private Boolean bonusAwarded = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}