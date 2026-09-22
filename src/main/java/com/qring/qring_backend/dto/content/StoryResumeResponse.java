package com.qring.qring_backend.dto.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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

    @JsonProperty("quiz_limit")
    private Integer quizLimit;

    @JsonProperty("can_extend")
    private Boolean canExtend;

    @Schema(description = "true 면 대화는 끝났고 보관/삭제 선택만 남은 상태")
    @JsonProperty("is_completed")
    private Boolean isCompleted;

    @Schema(description = "세션의 모델 티어: standard / premium", example = "standard")
    @JsonProperty("model_tier")
    private String modelTier;

    @Schema(description = "true 면 AI 응답을 아직 생성 중이다. 타임라인 마지막이 답이 없는 내 메시지일 수 있으니, "
            + "잠시 후 /story/resume 를 다시 호출하면 완성된 대화를 받는다. 보통은 서버가 기다렸다 주므로 false",
            example = "false")
    @JsonProperty("turn_in_progress")
    private Boolean turnInProgress;

    @Schema(description = "진행 중인 세션 전부 (최근 갱신 순). 위의 낱개 필드는 이 목록의 첫 번째 세션과 같은 값이다 "
            + "— 기존 앱과의 호환을 위해 남겨 둔 것이므로, 새 화면은 이 목록을 쓰면 된다")
    private List<Session> sessions;

    @Schema(description = "지금까지의 대화·퀴즈·채점 통합 기록 — /library/chat 의 timeline 과 같은 형식. 순서대로 렌더링하면 화면이 복원된다")
    private List<Map<String, Object>> timeline;

    /** 진행 중인 세션 하나. 목록으로 내려갈 때 쓴다. */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "진행 중인 스토리 세션 하나")
    public static class Session {

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

        @JsonProperty("quiz_limit")
        private Integer quizLimit;

        @JsonProperty("can_extend")
        private Boolean canExtend;

        @Schema(description = "true 면 대화는 끝났고 보관/삭제 선택만 남은 상태")
        @JsonProperty("is_completed")
        private Boolean isCompleted;

        @JsonProperty("model_tier")
        private String modelTier;

        @Schema(description = "true 면 이 세션의 AI 응답을 아직 생성 중이다")
        @JsonProperty("turn_in_progress")
        private Boolean turnInProgress;

        @Schema(description = "마지막으로 대화가 오간 시각. 목록 정렬·표시에 쓴다")
        @JsonProperty("updated_at")
        private LocalDateTime updatedAt;

        @Schema(description = "이 세션의 전체 타임라인. 목록에서 고른 세션을 그대로 복원할 수 있다")
        private List<Map<String, Object>> timeline;
    }
}
