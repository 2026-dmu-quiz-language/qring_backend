package com.qring.qring_backend.push.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qring.qring_backend.domain.user.UserDeviceToken;
import com.qring.qring_backend.domain.user.UserDeviceTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 기기 토큰 등록·해제·조회. 앱은 로그인 직후와 토큰 갱신(onTokenRefresh) 때 register 를, 로그아웃 때 unregister 를 부른다. */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushTokenService {

    private final UserDeviceTokenRepository tokenRepository;

    /**
     * 토큰 upsert. 이미 있는 토큰이면 소유자·플랫폼·lastSeenAt 만 갱신한다.
     * 같은 기기에서 다른 계정으로 로그인한 경우 소유자가 바뀌므로, 이전 계정으로는 더 이상 발송되지 않는다.
     */
    @Transactional
    public void register(Long userId, String token, String platform) {
        String normalized = token.trim();
        String plat = platform == null ? null : platform.trim().toUpperCase();
        LocalDateTime now = LocalDateTime.now();

        UserDeviceToken entity = tokenRepository.findByToken(normalized).orElseGet(() -> {
            UserDeviceToken t = new UserDeviceToken();
            t.setToken(normalized);
            return t;
        });
        if (entity.getUserId() != null && !entity.getUserId().equals(userId)) {
            log.info("[PUSH] 토큰 소유자 변경 userId {} -> {}", entity.getUserId(), userId);
        }
        entity.setUserId(userId);
        if (plat != null && !plat.isBlank()) {
            entity.setPlatform(plat);
        }
        entity.setLastSeenAt(now);
        tokenRepository.save(entity);
    }

    /** 로그아웃 등으로 토큰 해제. 본인 토큰이 아니거나 없으면 조용히 false. */
    @Transactional
    public boolean unregister(Long userId, String token) {
        int deleted = tokenRepository.deleteByUserIdAndToken(userId, token.trim());
        return deleted > 0;
    }

    /** 사용자 한 명의 토큰 문자열 목록. */
    @Transactional(readOnly = true)
    public List<String> tokensOf(Long userId) {
        return tokenRepository.findAllByUserId(userId).stream().map(UserDeviceToken::getToken).toList();
    }

    /** 여러 사용자의 토큰을 userId 별로 묶어서 (스케줄러가 N+1 조회를 피하도록). */
    @Transactional(readOnly = true)
    public Map<Long, List<String>> tokensByUser(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return tokenRepository.findAllByUserIdIn(userIds).stream()
                .collect(Collectors.groupingBy(UserDeviceToken::getUserId,
                        Collectors.mapping(UserDeviceToken::getToken, Collectors.toList())));
    }

    /** FCM 이 무효라고 답한 토큰 삭제. 비어 있으면 0. */
    @Transactional
    public int removeInvalid(Collection<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return 0;
        }
        int removed = tokenRepository.deleteAllByTokenIn(tokens);
        log.info("[PUSH] 무효 토큰 {}건 삭제", removed);
        return removed;
    }
}
