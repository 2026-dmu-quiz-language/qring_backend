package com.qring.qring_backend.mypage.service;

import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.domain.difficulty.DifficultyLevel;
import com.qring.qring_backend.domain.difficulty.DifficultyLevelRepository;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.domain.user.UserAsset;
import com.qring.qring_backend.domain.user.UserAssetRepository;
import com.qring.qring_backend.domain.user.UserStudyLogRepository;
import com.qring.qring_backend.domain.user.UserLanguageLevel;
import com.qring.qring_backend.domain.user.UserLanguageLevelRepository;
import com.qring.qring_backend.mypage.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UserRepository userRepository;
    private final UserAssetRepository userAssetRepository;
    private final DifficultyLevelRepository difficultyLevelRepository;
    private final UserStudyLogRepository userStudyLogRepository;
    private final UserLanguageLevelRepository userLanguageLevelRepository;
    private final PasswordEncoder passwordEncoder;

    public MyPageInfoResponse getMyPageInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));

        UserAsset asset = userAssetRepository.findByUserUserId(userId).orElse(null);
        Integer points = (asset != null && asset.getCurrentPoints() != null) ? asset.getCurrentPoints() : 0;

        Integer levelCode = user.getLevelCode();
        String levelDesc = null;
        if (levelCode != null) {
            levelDesc = difficultyLevelRepository.findById(levelCode)
                    .map(DifficultyLevel::getLevelDesc)
                    .orElse(null);
        }

        long consecutiveDays = computeConsecutiveDays(userId, user.getLanguage());

        return MyPageInfoResponse.builder()
                .nickname(user.getNickname())
                .levelCode(levelCode)
                .levelDesc(levelDesc)
                .points(points)
                .consecutiveDays(consecutiveDays)
                .language(user.getLanguage())
                .build();
    }

    public MyPageSettingResponse getMyPageSetting(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));

        return MyPageSettingResponse.builder()
                .id(user.getEmail())
                .pushEnabled(user.getPushEnabled() != null ? user.getPushEnabled() : true)
                .hasPassword(user.getPassword() != null && !user.getPassword().isEmpty())
                .build();
    }

    @Transactional
    public void updateMyPage(Long userId, MyPageUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));

        if (request.getNickname() != null && !request.getNickname().isEmpty()) {
            user.setNickname(request.getNickname());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getPushEnabled() != null) {
            user.setPushEnabled(request.getPushEnabled());
        }
        userRepository.save(user);
    }

    @Transactional
    public void updateLearning(Long userId, MyPageLearningRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));

        if (request.getLanguage() != null && !request.getLanguage().isEmpty()) {
            user.setLanguage(request.getLanguage());
        }
        if (request.getLevelCode() != null) {
            user.setLevelCode(request.getLevelCode());
        }
        userRepository.save(user);
        
        // Update or insert UserLanguageLevel
        if (request.getLanguage() != null && request.getLevelCode() != null) {
            UserLanguageLevel ull = userLanguageLevelRepository.findByUserIdAndLanguage(userId, request.getLanguage())
                    .orElse(new UserLanguageLevel());
            ull.setUserId(userId);
            ull.setLanguage(request.getLanguage());
            ull.setLevel(request.getLevelCode());
            userLanguageLevelRepository.save(ull);
        }
    }

    public boolean checkLanguage(Long userId, String language) {
        return userLanguageLevelRepository.findByUserIdAndLanguage(userId, language).isPresent();
    }

    @Transactional
    public void switchLanguage(Long userId, MyPageSwitchRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));

        if (request.getLanguage() != null && !request.getLanguage().isEmpty()) {
            user.setLanguage(request.getLanguage());
            
            // Sync levelCode with the selected language level if it exists
            userLanguageLevelRepository.findByUserIdAndLanguage(userId, request.getLanguage())
                    .ifPresent(ull -> user.setLevelCode(ull.getLevel()));
                    
            userRepository.save(user);
        }
    }

    private long computeConsecutiveDays(Long userId, String langCode) {
        if (langCode == null) return 0;
        List<LocalDate> dates = userStudyLogRepository.findDistinctStudyDatesDesc(userId, langCode);
        if (dates.isEmpty()) return 0;

        LocalDate today = LocalDate.now();
        LocalDate latest = dates.get(0);
        if (!latest.equals(today) && !latest.equals(today.minusDays(1))) return 0;

        long count = 1;
        for (int i = 1; i < dates.size(); i++) {
            if (dates.get(i).equals(dates.get(i - 1).minusDays(1))) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }
}
