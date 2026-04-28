package com.qring.qring_backend.domain.content;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Chapter")
@Getter @Setter @NoArgsConstructor
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chapter_id")
    private Long chapterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Column(name = "chapter_num")
    private Integer chapterNum;

    @Column(name = "chapter_title", length = 255)
    private String chapterTitle;

    @Column(name = "required_points")
    private Integer requiredPoints;
}