package com.qring.qring_backend.dashboard.controller;

import com.qring.qring_backend.dashboard.dto.DashboardResponse;
import com.qring.qring_backend.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 대시보드 HTTP 엔드포인트. 경로: {@code /api/v1/dash}. */
@RestController
@RequestMapping("/api/v1/dash")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /** 현재 로그인 사용자의 대시보드 데이터(진도율·연속일·코멘트 등) 조회. */
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(dashboardService.getDashboard(userId));
    }
}
