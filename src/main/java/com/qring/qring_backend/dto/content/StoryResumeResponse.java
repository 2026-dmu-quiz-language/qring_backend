package com.qring.qring_backend.dto.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "진행 중 스토리 세션 이어하기 응답 DTO")
public class StoryResumeResponse {

    @Schema(description = "이어할 세션이 있는지. false 면 나머지 필드는 모두 null", example = "true")
    @JsonProperty("has_session")
    private Boolean hasSession;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("character_name")
    private String characterName;

    private String situation;

    private String tone;

    @JsonProperty("target_language")
    private String targetLanguage;

    @JsonProperty("current_quiz_count")
    private Integer currentQuizCount;

    @Schema(description = "true 면 대화는 끝났고 보관/삭제 선택만 남은 상태")
    @JsonProperty("is_completed")
    private Boolean isCompleted;

    @Schema(description = "지금까지의 대화·퀴즈·채점 통합 기록 — /library/chat 의 timeline 과 같은 형식. 순서대로 렌더링하면 화면이 복원된다")
    private List<Map<String, Object>> timeline;
}
