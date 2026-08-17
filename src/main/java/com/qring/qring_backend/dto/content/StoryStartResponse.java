package com.qring.qring_backend.dto.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "실시간 스토리 세션 시작 응답 DTO")
public class StoryStartResponse {

    @Schema(description = "세션 ID (대화 주고받을 때 사용)", example = "sess-8a7b9c6d")
    @JsonProperty("session_id")
    private String sessionId;

    @Schema(description = "상대방 캐릭터 이름", example = "지민")
    @JsonProperty("character_name")
    private String characterName;

    @Schema(description = "상황 설명", example = "뉴욕 카페에서 아메리카노와 디저트를 주문하며 수다 떠는 상황")
    private String situation;

    @Schema(description = "AI의 첫 인사/오프닝 대사", example = "안녕! 오늘 카페 사람 정말 많다. 너 뭐 마실래?")
    @JsonProperty("ai_first_message")
    private String aiFirstMessage;

    @Schema(description = "첫 대사 한국어 번역/해석")
    @JsonProperty("ai_first_translation")
    private String aiFirstTranslation;

    @Schema(description = "사용자 남은 포인트 (-30pt 차감 후)", example = "70")
    @JsonProperty("user_remaining_points")
    private Integer userRemainingPoints;
}
