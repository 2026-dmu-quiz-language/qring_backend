package com.qring.qring_backend.domain.quiz;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 스토리 완료 기록 — (사용자, 콘텐츠, 언어) 당 한 줄이며 level 은 "마지막으로 완료한 레벨".
 * 레벨별 완료 여부는 이 테이블만으로는 알 수 없고 quiz_result(난이도·언어별 풀이 기록)로 판정한다
 * (QuestionResultService / ContentRepository 참고). DB 키 변경 없이 레벨 단위 완료를 지원하기 위한 구조.
 */
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

    /** 마지막으로 완료한 문제 세트의 난이도 (quiz_detail.difficulty). */
    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
