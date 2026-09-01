package com.qring.qring_backend.controller.content;

import com.qring.qring_backend.dto.content.StoryArchiveDetailResponse;
import com.qring.qring_backend.dto.content.StoryArchiveListResponse;
import com.qring.qring_backend.dto.content.StoryArchiveRequest;
import com.qring.qring_backend.dto.content.StoryArchiveResponse;
import com.qring.qring_backend.dto.content.StoryChatRequest;
import com.qring.qring_backend.dto.content.StoryChatResponse;
import com.qring.qring_backend.dto.content.StoryStartRequest;
import com.qring.qring_backend.dto.content.StoryStartResponse;
import com.qring.qring_backend.service.content.InteractiveStoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@Tag(name = "Interactive Story API", description = "실시간 턴 바이 턴 인터랙티브 스토리 & 퀴즈 API")
@RestController
@RequestMapping("/api/v1/story")
@RequiredArgsConstructor
public class InteractiveStoryController {

    private final InteractiveStoryService interactiveStoryService;

    @Operation(
            summary = "1단계: 실시간 스토리 세션 시작 및 포인트 차감 (-30pt)",
            description = "상대방 이름과 상황을 입력받아 포인트를 차감하고 스토리 세션(sessionId) 및 AI 첫 인사말을 발급합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/start")
    public ResponseEntity<StoryStartResponse> startStory(
            Authentication authentication,
            @Valid @RequestBody StoryStartRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("[InteractiveStoryController] 스토리 세션 시작 - userId: {}, character: {}, situation: {}",
                userId, request.getCharacterName(), request.getSituationDescription());

        StoryStartResponse response = interactiveStoryService.startStorySession(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "2단계: 실시간 대화 주고받기 / 퀴즈 답안 제출",
            description = "사용자가 대화 메시지나 퀴즈 정답을 보낼 때마다 AI가 실시간 반응하고, 대화 유도 또는 퀴즈(객관식/주관식/순서맞히기)를 출력합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/chat")
    public ResponseEntity<StoryChatResponse> chatTurn(
            Authentication authentication,
            @Valid @RequestBody StoryChatRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("[InteractiveStoryController] 턴 대화 요청 - userId: {}, sessionId: {}, userMsg: {}",
                userId, request.getSessionId(), request.getUserMessage());

        StoryChatResponse response = interactiveStoryService.processChatTurn(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "3단계: 완결된 스토리 영구 보관 (추가 포인트 결제)",
            description = "대화가 끝난 뒤 사용자가 보관을 선택하면 추가 포인트를 결제하고 스토리를 영구 저장합니다. 보관하지 않은 세션은 일정 시간 후 삭제됩니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/archive")
    public ResponseEntity<StoryArchiveResponse> archiveStory(
            Authentication authentication,
            @Valid @RequestBody StoryArchiveRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("[InteractiveStoryController] 스토리 보관 요청 - userId: {}, sessionId: {}", userId, request.getSessionId());
        return ResponseEntity.ok(interactiveStoryService.archiveStory(userId, request.getSessionId()));
    }

    @Operation(
            summary = "3단계: 스토리 보관하지 않고 삭제",
            description = "대화가 끝난 뒤 사용자가 보관하지 않기를 선택하면 세션을 즉시 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/discard")
    public ResponseEntity<Map<String, Object>> discardStory(
            Authentication authentication,
            @Valid @RequestBody StoryArchiveRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("[InteractiveStoryController] 스토리 삭제 요청 - userId: {}, sessionId: {}", userId, request.getSessionId());
        interactiveStoryService.discardStory(userId, request.getSessionId());
        return ResponseEntity.ok(Map.of("success", true, "message", "스토리가 삭제되었습니다."));
    }

    @Operation(
            summary = "보관된 스토리 목록 조회 (라이브러리)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/library")
    public ResponseEntity<StoryArchiveListResponse> getArchives(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(interactiveStoryService.getArchives(userId));
    }

    @Operation(
            summary = "보관된 스토리 대화 전문 조회 (다시 읽기)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/library/chat")
    public ResponseEntity<StoryArchiveDetailResponse> getArchiveDetail(
            Authentication authentication,
            @Valid @RequestBody StoryArchiveRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(interactiveStoryService.getArchiveDetail(userId, request.getSessionId()));
    }
}
