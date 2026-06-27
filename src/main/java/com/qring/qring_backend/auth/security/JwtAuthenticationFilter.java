package com.qring.qring_backend.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/** Authorization 헤더의 Bearer 토큰을 검증하고 SecurityContext에 인증 정보를 주입한다. */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    /** 요청당 1회 실행: Bearer 토큰 추출 → 검증 성공 시 principal=userId로 인증 컨텍스트 설정. */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // Authorization 헤더에서 "Bearer " 접두사를 떼고 토큰만 추출
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            // 서명/만료 검증에 통과하면 토큰에서 userId를 꺼내 인증 정보로 등록
            if (tokenProvider.validateToken(token)) {
                Long userId = tokenProvider.getUserIdFromToken(token);
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        // 다음 필터로 요청 전달 (토큰이 없거나 무효면 인증 없이 통과 후 뒤에서 차단)
        chain.doFilter(request, response);
    }
}
