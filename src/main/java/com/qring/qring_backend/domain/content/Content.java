package com.qring.qring_backend.domain.content;

import com.qring.qring_backend.domain.difficulty.DifficultyLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 학습 콘텐츠(스토리) 엔티티 — 카테고리·난이도·썸네일·총 챕터 수 보관. */
@Entity
@Table(name = "Content")
@Getter @Setter @NoArgsConstructor
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_id")
    private Long contentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ContentCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_code")
    private DifficultyLevel difficultyLevel;

    @Column(length = 255)
    private String title;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "total_chapters")
    private Integer totalChapters;

    @Column(length = 20)
    private String status;
}