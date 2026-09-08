package com.qring.qring_backend.domain.content;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 학습 콘텐츠(스토리) 엔티티 — 카테고리·썸네일 보관.
 * 잠금 여부(status)는 유저별 개인구매 개념이라 이 테이블에 고정 컬럼으로 두지 않고
 * user_content_unlock 테이블 존재 여부로 매 조회마다 동적으로 계산함.
 */
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

    @Column(length = 255)
    private String title;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "required_points")
    private Integer requiredPoints;

    @Column(name = "total_quiz_count")
    private Integer totalQuizCount;
}