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
@Schema(description = "나만의 인터랙티브 스토리 생성 요청 DTO")
public class InteractiveStoryCreateRequest {

    @Schema(description = "상대방 캐릭터 이름", example = "지민")
    @NotBlank(message = "상대방 이름을 입력해주세요.")
    private String characterName;

    @Schema(description = "대화 상황/주제 (버튼 태그 또는 자유 텍스트)", example = "카페에서 수다 떨기")
    @NotBlank(message = "원하는 상황을 입력해주세요.")
    private String situationDescription;

    @Schema(description = "대화 분위기/어조", example = "다정하게")
    @NotBlank(message = "대화 분위기를 선택해주세요.")
    private String tone;

    @Schema(description = "학습 대상 언어 (기본값: English)", example = "English")
    private String targetLanguage = "English";
}
