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
@Schema(description = "보관된 스토리 상세(다시 읽기) 응답 DTO")
public class StoryArchiveDetailResponse {

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("character_name")
    private String characterName;

    private String situation;

    private String tone;

    @JsonProperty("target_language")
    private String targetLanguage;

    @JsonProperty("quiz_count")
    private Integer quizCount;

    @JsonProperty("archived_at")
    private LocalDateTime archivedAt;

    @Schema(description = """
            대화·퀴즈·채점이 실제 진행 순서대로 섞인 통합 기록. type 으로 구분해 순서대로 렌더링하면 된다.
            - {type: "message", role: "user"|"assistant", content, translation(assistant만)}
            - {type: "quiz", quiz: {...}} — 직전 assistant 메시지와 함께 출제된 퀴즈
            - {type: "quiz_result", quiz_number, user_answer, result} — 직전 user 메시지가 제출한 답안의 채점 결과
            """)
    private List<Map<String, Object>> timeline;
}
