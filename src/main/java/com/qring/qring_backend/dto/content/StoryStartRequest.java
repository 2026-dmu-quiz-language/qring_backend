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

    @Schema(description = "학습 대상 언어", example = "English")
    private String targetLanguage = "English";
}
