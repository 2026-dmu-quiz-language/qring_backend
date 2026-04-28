package com.qring.qring_backend.domain.goal;

import java.time.LocalDate;

import com.qring.qring_backend.domain.content.Chapter;
import com.qring.qring_backend.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Daily_Goal_Record")
@Getter @Setter @NoArgsConstructor
public class DailyGoalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goal_record_id")
    private Long goalRecordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_chapter_id", nullable = false)
    private Chapter completedChapter;

    @Column(name = "achieve_date")
    private LocalDate achieveDate;

    @Column(name = "is_attained")
    private Boolean isAttained;
}