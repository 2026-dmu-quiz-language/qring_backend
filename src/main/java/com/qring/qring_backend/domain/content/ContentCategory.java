package com.qring.qring_backend.domain.content;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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