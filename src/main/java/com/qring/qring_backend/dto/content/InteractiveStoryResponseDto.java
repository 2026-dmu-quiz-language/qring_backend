package com.qring.qring_backend.dto.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "나만의 인터랙티브 스토리 생성 응답 DTO")
public class InteractiveStoryResponseDto {

    @Schema(description = "스토리 제목", example = "카페에서 수다 떨기")
    private String title;

    @Schema(description = "상대방 캐릭터 이름", example = "지민")
    @JsonProperty("character_name")
    private String characterName;

    @Schema(description = "대화 상황", example = "카페에서 만나 음료를 주문하고 근황을 나누는 상황")
    private String situation;

    @Schema(description = "총 포함된 퀴즈 개수", example = "5")
    @JsonProperty("total_quizzes")
    private Integer totalQuizzes;

    @Schema(description = "사용자 남은 포인트 (차감 후)", example = "70")
    @JsonProperty("user_remaining_points")
    private Integer userRemainingPoints;

    @Schema(description = "유동적 대화 및 퀴즈 흐름 (A-B-C...)")
    private List<Object> flow;
}
