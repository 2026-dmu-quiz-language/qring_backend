package com.qring.qring_backend.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "실시간 턴 바이 턴 스토리 세션 시작 요청 DTO")
public class StoryStartRequest {

    @Schema(description = "상대방 캐릭터 이름", example = "지민")
    @NotBlank(message = "상대방 이름을 입력해주세요.")
    private String characterName;

    @Schema(description = "대화 상황/주제 (자유 텍스트 입력 가능)", example = "뉴욕 카페에서 아메리카노와 디저트를 주문하며 수다 떠는 상황")
    @NotBlank(message = "원하는 상황을 입력해주세요.")
    private String situationDescription;

    @Schema(description = "대화 분위기/어조", example = "다정하게")
    @NotBlank(message = "대화 분위기를 선택해주세요.")
    private String tone;

    /**
     * @deprecated 학습 언어는 서버가 토큰의 사용자 설정(users.language: EN/JA/ZH)으로 정한다 (2026-09-22).
     *             프론트는 이 값을 알 수 없어 늘 비어 있었고, 그 결과 모든 세션이 영어로 진행되던 문제의 원인이었다.
     *             호환을 위해 필드만 남겨 두며, 값을 보내도 무시된다.
     */
    @Deprecated
    @Schema(description = "(무시됨) 학습 언어는 로그인 사용자의 설정 언어로 서버가 정합니다. 호환을 위해 필드만 남아 있습니다.",
            deprecated = true)
    private String targetLanguage;

    @Schema(description = "모델 티어. \"standard\"(기본 모델, 400pt) 또는 \"premium\"(프리미엄 모델, 550pt). 생략 시 standard",
            example = "premium", allowableValues = {"standard", "premium"})
    private String modelTier;
}
