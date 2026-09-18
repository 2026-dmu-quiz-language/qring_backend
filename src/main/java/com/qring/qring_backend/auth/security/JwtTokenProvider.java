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

    /** 비밀번호 재설정 토큰 유효 시간 (기본 10분). 코드 검증 후 새 비밀번호 입력까지의 창. */
    @Value("${jwt.reset-token-validity-ms:600000}")
    private long resetTokenValidityMs;

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";
    public static final String TYPE_RESET = "reset";

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /** 액세스 토큰 발급 (기본 1시간). */
    public String generateAccessToken(Long userId) {
        return generateToken(userId, accessTokenValidityMs, TYPE_ACCESS);
    }

    /** 리프레시 토큰 발급 (기본 14일). */
    public String generateRefreshToken(Long userId) {
        return generateToken(userId, refreshTokenValidityMs, TYPE_REFRESH);
    }

    /** 비밀번호 재설정 토큰 발급 (기본 10분). 인증 필터는 이 타입을 인증 수단으로 받지 않는다. */
    public String generateResetToken(Long userId) {
        return generateToken(userId, resetTokenValidityMs, TYPE_RESET);
    }

    public long getResetTokenValidityMs() {
        return resetTokenValidityMs;
    }

    /** 토큰의 type 클레임 (access / refresh / reset). 검증 실패 시 null. */
    public String getTokenType(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            Object type = claims.get("type");
            return type == null ? null : String.valueOf(type);
        } catch (Exception e) {
            return null;
        }
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

    /** 서명은 유효하지만 만료된 토큰이면 true. 그 외(위조·형식 오류·유효)는 false. */
    public boolean isExpired(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return false;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
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
