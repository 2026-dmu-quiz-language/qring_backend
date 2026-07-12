package com.qring.qring_backend.domain.user;

import com.qring.qring_backend.domain.content.Content;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

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

    @Column(name = "progress_rate")
    private Integer progressRate;

    @Column(length = 10)
    private String language;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}