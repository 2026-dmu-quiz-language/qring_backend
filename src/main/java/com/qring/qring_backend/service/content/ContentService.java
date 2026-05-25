package com.qring.qring_backend.service.content;

import com.qring.qring_backend.domain.content.ContentRepository;
import com.qring.qring_backend.dto.content.ContentListResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;

    public List<ContentListResponseDto> getContentList(Long userId) {
        return contentRepository.findContentListByUserId(userId);
    }
}
