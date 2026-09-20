package com.qring.qring_backend.domain.competition;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 봇 컴피티션 문제 — 언어 무관 필드(세트, 레벨, 원본 순번, 문제 유형).
 * 문제 세트 파일(_01, _02 …)마다 id 가 1 부터 다시 시작하므로 set_key 가 없으면 (level, origin_id) 가 충돌한다.
 * set_key 는 import 요청 파라미터(quizSet)로 받는다 — 파일 안의 quizSet 필드는 세트마다 같은 값이라 못 쓴다.
 */
@Entity
@Table(
        name = "competition_quiz_detail",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_set_level_origin",
                columnNames = {"set_key", "level", "origin_id"}
        )
)
@Getter @Setter @NoArgsConstructor
public class CompetitionQuizDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id")
    private Long quizId;

    /** 문제 세트 식별자 (예: "01", "02"). import 시 quizSet 파라미터, 생략하면 "default". */
    @Column(name = "set_key", length = 30, nullable = false)
    private String setKey;

    @Column(name = "level", nullable = false)
    private Integer level;

    /** json 원본의 id (세트·레벨 내 순번). 같은 세트의 다른 언어 파일은 같은 id 로 이 row 에 붙는다. */
    @Column(name = "origin_id", nullable = false)
    private Integer originId;

    /**
     * 처음 import 된 언어의 문제 유형. 언어별 파일은 같은 id 가 같은 문제가 아니라 다른 언어와 다를 수 있으므로
     * 출제는 이 값이 아니라 본문 모양(CompetitionMatchService.effectiveType)으로 유형을 정한다.
     */
    @Column(name = "quiz_type", length = 30, nullable = false)
    private String quizType;
}
