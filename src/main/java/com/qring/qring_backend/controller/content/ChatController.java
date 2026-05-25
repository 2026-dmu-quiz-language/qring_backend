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
    public ResponseEntity<ChatResponseDto> getChat(@RequestParam Long contentId) {
        ChatResponseDto response = chatService.getChatData(contentId);
        return ResponseEntity.ok(response);
    }
}
