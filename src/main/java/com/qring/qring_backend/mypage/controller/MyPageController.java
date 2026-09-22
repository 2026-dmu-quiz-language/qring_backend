package com.qring.qring_backend.mypage.controller;

import com.qring.qring_backend.mypage.dto.*;
import com.qring.qring_backend.mypage.service.MyPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @PostMapping("/mypage")
    public ResponseEntity<MyPageInfoResponse> getMyPage(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(myPageService.getMyPageInfo(userId));
    }

    @PostMapping("/mypage/setting")
    public ResponseEntity<MyPageSettingResponse> getMyPageSetting(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(myPageService.getMyPageSetting(userId));
    }

    @PostMapping("/update") // Using /update as requested, though usually /mypage/update is better
    public ResponseEntity<Void> updateMyPage(Authentication authentication, @RequestBody MyPageUpdateRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("[MyPageController] /update 호출됨. userId: {}", userId);
        log.info("[MyPageController] Request Payload - nickname: {}, pushEnabled: {}, passwordIsPresent: {}", 
                 request.getNickname(), request.getPushEnabled(), (request.getPassword() != null && !request.getPassword().isEmpty()));
                 
        myPageService.updateMyPage(userId, request);
        
        log.info("[MyPageController] /update 성공적으로 완료됨. userId: {}", userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mypage/learning")
    public ResponseEntity<Void> updateLearning(Authentication authentication, @RequestBody MyPageLearningRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        myPageService.updateLearning(userId, request);
        return ResponseEntity.ok().build();
    }

    /** 현재 학습 언어와 해금된 언어 목록을 함께 내려준다. lang 파라미터는 더 이상 쓰지 않는다. */
    @PostMapping("/langcheck")
    public ResponseEntity<LanguageStatusResponse> checkLanguage(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(myPageService.getLanguageStatus(userId));
    }

    /** 학습 언어를 전환하고, 전환 후 확정된 상태를 그대로 돌려준다. */
    @PostMapping("/switch")
    public ResponseEntity<LanguageStatusResponse> switchLanguage(Authentication authentication, @RequestBody MyPageSwitchRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(myPageService.switchLanguage(userId, request));
    }
}
