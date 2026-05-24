package com.qring.qring_backend.controller.content;

import com.qring.qring_backend.dto.content.ContentListResponseDto;
import com.qring.qring_backend.service.content.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @PostMapping("/contentList")
    public ResponseEntity<List<ContentListResponseDto>> getContentList(
            @RequestHeader("Authorization") String token) {

        // TODO: 태형님 토큰 방식 확정되면 token에서 userId 추출하는 로직으로 교체
        Long userId = extractUserIdFromToken(token);

        List<ContentListResponseDto> contentList = contentService.getContentList(userId);
        return ResponseEntity.ok(contentList);
    }

    private Long extractUserIdFromToken(String token) {
        // TODO: 토큰 방식 확정 후 구현
        throw new UnsupportedOperationException("토큰 파싱 미구현");
    }
}
