package com.qring.qring_backend.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/** JWT 액세스/리프레시 토큰 생성·검증·subject(userId) 추출. */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-validity-ms:3600000}")
    private long accessTokenValidityMs;

    @Value("${jwt.refresh-token-validity-ms:1209600000}")
    private long refreshTokenValidityMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /** 액세스 토큰 발급 (기본 1시간). */
    public String generateAccessToken(Long userId) {
        return generateToken(userId, accessTokenValidityMs, "access");
    }

    /** 리프레시 토큰 발급 (기본 14일). */
    public String generateRefreshToken(Long userId) {
        return generateToken(userId, refreshTokenValidityMs, "refresh");
    }

    private String generateToken(Long userId, long validityMs, String type) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityMs);
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("type", type)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact();
    }

    /** 서명/만료 검증. 예외 발생 시 false. */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** 토큰 subject 클레임을 userId(Long)로 변환해 반환. */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).getPayload();
        return Long.parseLong(claims.getSubject());
    }
}
