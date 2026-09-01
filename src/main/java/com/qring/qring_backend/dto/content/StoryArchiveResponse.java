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
@Schema(description = "스토리 보관 결제 응답 DTO")
public class StoryArchiveResponse {

    @Schema(description = "세션 ID")
    @JsonProperty("session_id")
    private String sessionId;

    @Schema(description = "사용자 남은 포인트")
    @JsonProperty("user_remaining_points")
    private Integer userRemainingPoints;
}
