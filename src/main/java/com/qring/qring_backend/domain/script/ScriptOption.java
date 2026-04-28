package com.qring.qring_backend.domain.script;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Script_Option")
@Getter @Setter @NoArgsConstructor
public class ScriptOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long optionId;

    // 이 선택지가 속한 스크립트
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "script_id", nullable = false)
    private Script script;

    // 선택 시 이동할 다음 스크립트
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_script_id")
    private Script nextScript;

    @Column(name = "option_text", length = 500)
    private String optionText;

    @Column(name = "ending_score")
    private Integer endingScore;
}