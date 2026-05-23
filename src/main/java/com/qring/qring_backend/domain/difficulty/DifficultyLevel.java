package com.qring.qring_backend.domain.difficulty;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 난이도 등급 — levelCode(1~N)와 표시명/설명 보관. */
@Entity
@Table(name = "Difficulty_Level")
@Getter @Setter @NoArgsConstructor
public class DifficultyLevel {

    @Id
    @Column(name = "level_code")
    private Integer levelCode;

    @Column(name = "level_name", nullable = false, length = 20)
    private String levelName;

    @Column(name = "level_desc", length = 255)
    private String levelDesc;
}