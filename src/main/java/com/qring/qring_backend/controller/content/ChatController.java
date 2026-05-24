package com.qring.qring_backend.controller.content;

import com.qring.qring_backend.dto.content.ChatResponseDto;
import com.qring.qring_backend.service.content.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponseDto> getChat(
            @RequestHeader("Authorization") String token,
            @RequestParam Long contentId) {

        // TODO: 태형님 토큰 방식 확정되면 token 검증 로직 추가
        ChatResponseDto response = chatService.getChatData(contentId);
        return ResponseEntity.ok(response);
    }
}
