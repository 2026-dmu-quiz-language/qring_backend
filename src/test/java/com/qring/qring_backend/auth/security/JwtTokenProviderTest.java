package com.qring.qring_backend.auth.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 재설정 토큰: 타입 클레임, 만료 판별, userId 추출. */
class JwtTokenProviderTest {

    private static JwtTokenProvider provider(long resetValidityMs) throws Exception {
        JwtTokenProvider p = new JwtTokenProvider();
        set(p, "secret", "qring-test-secret-key-please-change-in-production-min-32-bytes");
        set(p, "accessTokenValidityMs", 3600000L);
        set(p, "refreshTokenValidityMs", 1209600000L);
        set(p, "resetTokenValidityMs", resetValidityMs);
        p.init();
        return p;
    }

    private static void set(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    @DisplayName("재설정 토큰은 type=reset 이고 userId 를 담는다")
    void resetTokenHasResetType() throws Exception {
        JwtTokenProvider p = provider(600000L);
        String reset = p.generateResetToken(42L);

        assertTrue(p.validateToken(reset));
        assertEquals(JwtTokenProvider.TYPE_RESET, p.getTokenType(reset));
        assertEquals(42L, p.getUserIdFromToken(reset));
        assertEquals(JwtTokenProvider.TYPE_ACCESS, p.getTokenType(p.generateAccessToken(42L)));
        assertEquals(JwtTokenProvider.TYPE_REFRESH, p.getTokenType(p.generateRefreshToken(42L)));
        assertFalse(p.isExpired(reset));
    }

    @Test
    @DisplayName("만료된 토큰은 validateToken 이 false 이고 isExpired 가 true, 위조 토큰은 둘 다 false")
    void expiredVersusInvalid() throws Exception {
        JwtTokenProvider p = provider(-1000L); // 발급 즉시 만료
        String expired = p.generateResetToken(7L);
        assertFalse(p.validateToken(expired));
        assertTrue(p.isExpired(expired));
        assertNull(p.getTokenType(expired));

        assertFalse(p.validateToken("not.a.token"));
        assertFalse(p.isExpired("not.a.token"));
        assertNull(p.getTokenType("not.a.token"));
    }
}
