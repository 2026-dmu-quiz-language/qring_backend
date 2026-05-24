package com.qring.qring_backend.domain.content;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 콘텐츠 카테고리 분류 — 표시 순서로 정렬. */
@Entity
@Table(name = "Content_Category")
@Getter @Setter @NoArgsConstructor
public class ContentCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;

    @Column(name = "display_order")
    private Integer displayOrder;
}