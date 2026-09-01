package com.qring.qring_backend.dto.content;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@Schema(description = "스토리 보관/삭제/상세 조회 요청 DTO")
public class StoryArchiveRequest {

    @Schema(description = "세션 ID", example = "sess-8a7b9c6d-1e2f-4a3b-9c8d-7e6f5a4b3c2d")
    @JsonProperty("session_id")
    @NotBlank(message = "세션 ID가 필요합니다.")
    private String sessionId;
}
