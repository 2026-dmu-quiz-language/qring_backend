package com.qring.qring_backend.service.content;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.domain.content.ContentRepository;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.dto.content.ContentListResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final UserRepository userRepository;

    @Value("${server.base-url}")
    private String baseUrl;

    public List<ContentListResponseDto> getContentList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        String language = user.getLanguage();

        return contentRepository.findContentListByUserId(userId, language).stream()
                .map(dto -> new ContentListResponseDto(
                        dto.getContentId(),
                        dto.getCategoryName(),
                        baseUrl + dto.getThumbnailUrl(),
                        dto.getTitle(),
                        dto.getQuizCount(),
                        dto.getIsCompleted(),
                        dto.getStatus(),
                        dto.getRequiredPoints()
                ))
                .toList();
    }
}