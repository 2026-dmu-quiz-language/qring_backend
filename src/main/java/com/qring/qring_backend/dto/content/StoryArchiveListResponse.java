package com.qring.qring_backend.dto.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "보관된 스토리 목록 응답 DTO")
public class StoryArchiveListResponse {

    private List<ArchiveSummary> archives;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArchiveSummary {

        @JsonProperty("session_id")
        private String sessionId;

        @JsonProperty("character_name")
        private String characterName;

        @Schema(description = "상황 설명 (목록 제목으로 사용)")
        private String situation;

        @JsonProperty("quiz_count")
        private Integer quizCount;

        @JsonProperty("archived_at")
        private LocalDateTime archivedAt;
    }
}
