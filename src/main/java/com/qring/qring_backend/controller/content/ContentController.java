package com.qring.qring_backend.controller.content;

import com.qring.qring_backend.dto.content.ContentListResponseDto;
import com.qring.qring_backend.service.content.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @PostMapping("/contentList")
    public ResponseEntity<List<ContentListResponseDto>> getContentList(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<ContentListResponseDto> contentList = contentService.getContentList(userId);
        return ResponseEntity.ok(contentList);
    }
}
