package com.qring.qring_backend.domain.difficulty;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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