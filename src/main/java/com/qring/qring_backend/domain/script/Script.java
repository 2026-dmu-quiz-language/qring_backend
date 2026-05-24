package com.qring.qring_backend.domain.script;

import com.qring.qring_backend.domain.content.Chapter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 챕터 내 스크립트 한 줄 — 등장 인물 대사와 선택지(옵션) 연결. */
@Entity
@Table(name = "Script")
@Getter @Setter @NoArgsConstructor
public class Script {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "script_id")
    private Long scriptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Column(name = "sequence_num")
    private Integer sequenceNum;

    @Column(name = "character_name", length = 50)
    private String characterName;

    @Column(name = "script_content", columnDefinition = "TEXT")
    private String scriptContent;

    @Column(name = "has_options")
    private Boolean hasOptions;

    // 선택지가 있을 경우 연결되는 ScriptOption (단방향)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private ScriptOption scriptOption;
}