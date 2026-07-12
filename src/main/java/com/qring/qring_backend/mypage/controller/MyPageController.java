package com.qring.qring_backend.mypage.controller;

import com.qring.qring_backend.mypage.dto.*;
import com.qring.qring_backend.mypage.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
        myPageService.updateMyPage(userId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mypage/learning")
    public ResponseEntity<Void> updateLearning(Authentication authentication, @RequestBody MyPageLearningRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        myPageService.updateLearning(userId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/langcheck")
    public ResponseEntity<Boolean> checkLanguage(Authentication authentication, @RequestParam("lang") String lang) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(myPageService.checkLanguage(userId, lang));
    }

    @PostMapping("/switch")
    public ResponseEntity<Void> switchLanguage(Authentication authentication, @RequestBody MyPageSwitchRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        myPageService.switchLanguage(userId, request);
        return ResponseEntity.ok().build();
    }
}
