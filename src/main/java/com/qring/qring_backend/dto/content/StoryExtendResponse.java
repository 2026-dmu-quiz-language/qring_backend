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
@Schema(description = "스토리 이어하기(연장) 응답 DTO")
public class StoryExtendResponse {

    @JsonProperty("session_id")
    private String sessionId;

    @Schema(description = "마무리된 장면을 다시 여는 AI의 연결 대사 — 채팅에 이어서 렌더링하면 된다")
    @JsonProperty("ai_message")
    private String aiMessage;

    private String translation;

    @JsonProperty("current_quiz_count")
    private Integer currentQuizCount;

    @Schema(description = "늘어난 퀴즈 한도 (예: 첫 연장 후 10, 최대 15)")
    @JsonProperty("quiz_limit")
    private Integer quizLimit;

    @Schema(description = "추가 이어하기 가능 여부 (2회 상한 도달 시 false)")
    @JsonProperty("can_extend")
    private Boolean canExtend;

    @JsonProperty("user_remaining_points")
    private Integer userRemainingPoints;
}
