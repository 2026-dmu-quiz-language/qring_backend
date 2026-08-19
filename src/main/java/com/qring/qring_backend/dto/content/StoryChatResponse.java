package com.qring.qring_backend.dto.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "실시간 턴 바이 턴 메시지/퀴즈 응답 DTO")
public class StoryChatResponse {

    @Schema(description = "세션 ID", example = "sess-8a7b9c6d-1e2f-4a3b-9c8d-7e6f5a4b3c2d")
    @JsonProperty("session_id")
    private String sessionId;

    @Schema(description = "AI의 대답/반응 대사", example = "창가 쪽이 햇살이 참 예쁘다! 아까 주문할 때 보니 신메뉴 케이크가 나왔더라고.")
    @JsonProperty("ai_message")
    private String aiMessage;

    @Schema(description = "AI 대사 한국어 번역/해석")
    private String translation;

    @Schema(description = "이번 턴에 퀴즈 출제 여부", example = "true")
    @JsonProperty("is_quiz")
    private Boolean isQuiz;

    @Schema(description = "출제된 퀴즈 객체 (is_quiz = true일 때만 노출)")
    private Map<String, Object> quiz;

    @Schema(description = "직전 턴에 출제된 퀴즈에 대한 채점 결과. 대기 중인 퀴즈가 없던 턴은 항상 none",
            example = "none", allowableValues = {"correct", "incorrect", "none"})
    @JsonProperty("answer_result")
    private String answerResult;

    @Schema(description = "현재까지 출제된 누적 퀴즈 개수 (총 5개 중)", example = "1")
    @JsonProperty("current_quiz_count")
    private Integer currentQuizCount;

    @Schema(description = "스토리 완전 종료 여부", example = "false")
    @JsonProperty("is_completed")
    private Boolean isCompleted;
}
