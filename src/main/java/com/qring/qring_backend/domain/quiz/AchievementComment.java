package com.qring.qring_backend.domain.quiz;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 진도율 구간(minRate~maxRate)별 성취 코멘트 문구. 대시보드 노출용. */
@Entity
@Table(name = "achievement_comment")
@Getter
@NoArgsConstructor
public class AchievementComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @Column(name = "min_rate", nullable = false)
    private int minRate;

    @Column(name = "max_rate", nullable = false)
    private int maxRate;

    @Column(name = "comment_text", nullable = false, length = 255)
    private String commentText;
}