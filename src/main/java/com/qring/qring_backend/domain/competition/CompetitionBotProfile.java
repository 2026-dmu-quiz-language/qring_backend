package com.qring.qring_backend.domain.competition;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 레벨별 봇 정답률 프로필. 속도(타이밍)는 프론트에서 제어하므로 여기 저장하지 않음. */
@Entity
@Table(name = "competition_bot_profile")
@Getter @Setter @NoArgsConstructor
public class CompetitionBotProfile {

    @Id
    @Column(name = "level")
    private Integer level;

    @Column(name = "correct_rate", nullable = false)
    private Integer correctRate;
}