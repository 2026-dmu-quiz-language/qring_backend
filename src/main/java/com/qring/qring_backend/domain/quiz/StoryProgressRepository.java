package com.qring.qring_backend.domain.quiz;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryProgressRepository extends JpaRepository<StoryProgress, Long> {

    Optional<StoryProgress> findByUserIdAndContentIdAndLanguage(
            Long userId, Long contentId, String language);

    // 대시보드: 언어별 완료 스토리 수
    int countByUserIdAndLanguageAndIsCompleted(Long userId, String language, Boolean isCompleted);

    // 콘텐츠 목록: 유저의 언어별 완료 스토리 목록
    List<StoryProgress> findByUserIdAndLanguage(Long userId, String language);
}
