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
@Schema(description = "실시간 턴 바이 턴 메시지/퀴즈 제출 요청 DTO")
public class StoryChatRequest {

    @Schema(description = "세션 ID", example = "sess-8a7b9c6d")
    @JsonProperty("session_id")
    @NotBlank(message = "세션 ID가 필요합니다.")
    private String sessionId;

    @Schema(description = "사용자의 대답 / 퀴즈 입력 텍스트", example = "Sounds great! Let's sit there.")
    @JsonProperty("user_message")
    @NotBlank(message = "메시지 또는 퀴즈 답을 입력해주세요.")
    private String userMessage;
}
