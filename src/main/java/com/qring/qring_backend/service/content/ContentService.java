package com.qring.qring_backend.service.content;

import com.qring.qring_backend.domain.content.ContentRepository;
import com.qring.qring_backend.dto.content.ContentListResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;

    @Value("${server.base-url}")
    private String baseUrl;

    public List<ContentListResponseDto> getContentList(Long userId) {
        return contentRepository.findContentListByUserId(userId).stream()
                .map(dto -> new ContentListResponseDto(
                        dto.getCategoryName(),
                        baseUrl + dto.getThumbnailUrl(),
                        dto.getTitle(),
                        dto.getContentId(),
                        dto.getQuizCount(),
                        dto.getIsCompleted(),
                        dto.getStatus()
                ))
                .toList();
    }
}
