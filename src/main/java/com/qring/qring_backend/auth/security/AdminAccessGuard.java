package com.qring.qring_backend.auth.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 관리자 전용 API 접근 통제.
 *
 * 프로젝트에 역할(role) 체계가 없어 /admin/** 이 로그인만 하면 누구나 호출 가능했다
 * (예: 일반 유저가 컴피티션 출제 문제를 임의로 주입 가능). DB 스키마 변경 없이 막기 위해
 * 환경변수 ADMIN_USER_IDS (쉼표 구분 userId 목록) 에 등록된 사용자만 허용한다.
 *
 * 설정이 비어 있으면 아무도 통과하지 못한다 (안전 기본값).
 * 사용법: .env 에 ADMIN_USER_IDS=1,2 형태로 팀 관리자 계정의 userId 를 등록.
 */
@Component
public class AdminAccessGuard {

    private final Set<Long> adminUserIds;

    public AdminAccessGuard(@Value("${qring.admin.user-ids:}") String adminUserIdsCsv) {
        this.adminUserIds = Arrays.stream(adminUserIdsCsv.split(","))
                .map(String::trim)
                .filter(s -> s.matches("\\d+"))
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }

    /** 관리자가 아니면 예외. GlobalExceptionHandler 를 거쳐 400 FORBIDDEN_ADMIN_ONLY 로 응답된다. */
    public void checkAdmin(Long userId) {
        if (userId == null || !adminUserIds.contains(userId)) {
            throw new IllegalArgumentException("FORBIDDEN_ADMIN_ONLY");
        }
    }
}
