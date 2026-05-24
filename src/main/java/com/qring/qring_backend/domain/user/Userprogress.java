package com.qring.qring_backend.domain.user;

import com.qring.qring_backend.domain.content.Chapter;
import com.qring.qring_backend.domain.content.Content;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/** 사용자별 콘텐츠 진행 상태 — 최근 챕터와 진도율(%) 보관. */
@Entity
@Table(name = "User_Progress")
@Getter @Setter @NoArgsConstructor
public class Userprogress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Long progressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_chapter_id")
    private Chapter lastChapter;

    @Column(name = "progress_rate")
    private Integer progressRate;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}