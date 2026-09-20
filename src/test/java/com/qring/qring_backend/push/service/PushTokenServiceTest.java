package com.qring.qring_backend.push.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.qring.qring_backend.domain.user.UserDeviceToken;
import com.qring.qring_backend.domain.user.UserDeviceTokenRepository;

/** 기기 토큰 upsert: 새 토큰은 생성, 있는 토큰은 소유자를 현재 사용자로 바꾼다 (기기 공유·계정 전환). */
class PushTokenServiceTest {

    private UserDeviceTokenRepository repository;
    private PushTokenService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserDeviceTokenRepository.class);
        service = new PushTokenService(repository);
    }

    private static UserDeviceToken token(Long userId, String token) {
        UserDeviceToken t = new UserDeviceToken();
        t.setUserId(userId);
        t.setToken(token);
        t.setLastSeenAt(LocalDateTime.now().minusDays(1));
        return t;
    }

    @Test
    @DisplayName("처음 보는 토큰은 새 row 로 저장하고 platform 은 대문자로 정규화한다")
    void register_newToken() {
        when(repository.findByToken("abc")).thenReturn(Optional.empty());

        service.register(7L, "  abc ", "android");

        ArgumentCaptor<UserDeviceToken> saved = ArgumentCaptor.forClass(UserDeviceToken.class);
        verify(repository).save(saved.capture());
        assertEquals(7L, saved.getValue().getUserId());
        assertEquals("abc", saved.getValue().getToken());
        assertEquals("ANDROID", saved.getValue().getPlatform());
        assertNotNull(saved.getValue().getLastSeenAt());
    }

    @Test
    @DisplayName("다른 사용자 소유였던 토큰을 등록하면 소유자가 현재 사용자로 바뀐다 (같은 기기 다른 계정)")
    void register_existingToken_reassignsOwner() {
        UserDeviceToken existing = token(1L, "abc");
        existing.setPlatform("IOS");
        when(repository.findByToken("abc")).thenReturn(Optional.of(existing));

        service.register(2L, "abc", null);

        verify(repository).save(existing);
        assertEquals(2L, existing.getUserId());
        assertEquals("IOS", existing.getPlatform()); // platform 생략 시 기존 값 유지
        assertTrue(existing.getLastSeenAt().isAfter(LocalDateTime.now().minusMinutes(1)));
    }

    @Test
    @DisplayName("unregister 는 본인 토큰만 지우고 삭제 건수로 true/false 를 돌려준다")
    void unregister() {
        when(repository.deleteByUserIdAndToken(7L, "abc")).thenReturn(1);
        when(repository.deleteByUserIdAndToken(7L, "not-mine")).thenReturn(0);

        assertTrue(service.unregister(7L, " abc "));
        assertFalse(service.unregister(7L, "not-mine"));
    }

    @Test
    @DisplayName("tokensByUser 는 한 번의 IN 조회로 userId 별 토큰 목록을 만든다")
    void tokensByUser_groupsByUser() {
        when(repository.findAllByUserIdIn(Set.of(1L, 2L)))
                .thenReturn(List.of(token(1L, "a"), token(1L, "b"), token(2L, "c")));

        Map<Long, List<String>> result = service.tokensByUser(Set.of(1L, 2L));

        assertEquals(List.of("a", "b"), result.get(1L));
        assertEquals(List.of("c"), result.get(2L));
    }

    @Test
    @DisplayName("removeInvalid 는 빈 목록이면 DB 를 건드리지 않는다")
    void removeInvalid_emptyIsNoop() {
        assertEquals(0, service.removeInvalid(List.of()));
        verify(repository, never()).deleteAllByTokenIn(any());
    }
}
