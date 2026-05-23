package com.qring.qring_backend.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Set;

/** Google/Kakao/LINE 외부 OAuth 제공자와의 통신 및 토큰 검증. */
@Slf4j
@Service
public class OAuthService {

    @Value("${oauth.google.client-id:}")
    private String googleClientId;

    @Value("${oauth.kakao.app-key:}")
    private String kakaoAppKey;

    @Value("${oauth.kakao.client-secret:}")
    private String kakaoClientSecret;

    @Value("${oauth.line.channel-id:}")
    private String lineChannelId;

    @Value("${oauth.line.channel-secret:}")
    private String lineChannelSecret;

    private final RestTemplate rest = new RestTemplate();

    private static final Set<String> GOOGLE_ISSUERS = Set.of(
        "accounts.google.com", "https://accounts.google.com"
    );

    /** Google ID 토큰 검증 (tokeninfo 엔드포인트). aud/iss/email_verified 확인 후 페이로드 반환. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> verifyGoogleToken(String idToken) {
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
        try {
            ResponseEntity<Map> response = rest.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null) throw new IllegalArgumentException("SOCIAL_LOGIN_FAILED");

            if (!googleClientId.equals(body.get("aud"))) {
                log.warn("[GOOGLE] aud mismatch. expected={}, got={}", googleClientId, body.get("aud"));
                throw new IllegalArgumentException("SOCIAL_LOGIN_FAILED");
            }
            String iss = (String) body.get("iss");
            if (iss == null || !GOOGLE_ISSUERS.contains(iss)) {
                log.warn("[GOOGLE] iss mismatch: {}", iss);
                throw new IllegalArgumentException("SOCIAL_LOGIN_FAILED");
            }
            if (!"true".equals(String.valueOf(body.get("email_verified")))) {
                log.warn("[GOOGLE] email not verified");
                throw new IllegalArgumentException("SOCIAL_LOGIN_FAILED");
            }
            return body;
        } catch (IllegalArgumentException e) { throw e; }
        catch (HttpStatusCodeException e) {
            log.error("[GOOGLE] HTTP {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalArgumentException("SOCIAL_LOGIN_FAILED");
        }
        catch (Exception e) {
            log.error("[GOOGLE] unexpected", e);
            throw new IllegalArgumentException("SOCIAL_LOGIN_FAILED");
        }
    }

    /** Kakao authorization code → access token → user info. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> verifyKakaoCode(String code, String redirectUri) {
        log.info("[KAKAO] start. appKey configured={}, redirectUri={}, code(len)={}",
            kakaoAppKey != null && !kakaoAppKey.isBlank(),
            redirectUri,
            code == null ? 0 : code.length());
        try {
            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", kakaoAppKey);
            params.add("redirect_uri", redirectUri);
            params.add("code", code);
            if (kakaoClientSecret != null && !kakaoClientSecret.isBlank()) {
                params.add("client_secret", kakaoClientSecret);
            }

            ResponseEntity<Map> tokenRes = rest.postForEntity(
                "https://kauth.kakao.com/oauth/token",
                new HttpEntity<>(params, h), Map.class);
            Map<String, Object> tokenBody = tokenRes.getBody();
            log.info("[KAKAO] token response status={}, hasAccessToken={}",
                tokenRes.getStatusCode(),
                tokenBody != null && tokenBody.get("access_token") != null);
            if (tokenBody == null || tokenBody.get("access_token") == null)
                throw new IllegalArgumentException("SOCIAL_LOGIN_FAILED");

            HttpHeaders uh = new HttpHeaders();
            uh.setBearerAuth((String) tokenBody.get("access_token"));
            ResponseEntity<Map> ures = rest.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET, new HttpEntity<>(uh), Map.class);
            Map<String, Object> body = ures.getBody();
            if (body == null) throw new IllegalArgumentException("SOCIAL_LOGIN_FAILED");
            log.info("[KAKAO] user profile ok. id={}", body.get("id"));
            return body;
        } catch (IllegalArgumentException e) { throw e; }
        catch (HttpStatusCodeException e) {
            log.error("[KAKAO] HTTP {} - body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalArgumentException("SOCIAL_LOGIN_FAILED");
        }
        catch (Exception e) {
            log.error("[KAKAO] unexpected", e);
            throw new IllegalArgumentException("SOCIAL_LOGIN_FAILED");
        }
    }

    /** LINE authorization code → access token → user profile. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> verifyLineCode(String code, String redirectUri) {
        log.info("[LINE] start. channelIdConfigured={}, redirectUri={}",
            lineChannelId != null && !lineChannelId.isBlank(), redirectUri);
        try {
            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("code", code);
            params.add("redirect_uri", redirectUri);
            params.add("client_id", lineChannelId);
            params.add("client_secret", lineChannelSecret);

            ResponseEntity<Map> tokenRes = rest.postForEntity(
                "https://api.line.me/oauth2/v2.1/token",
                new HttpEntity<>(params, h), Map.class);
            Map<String, Object> tokenBody = tokenRes.getBody();
            if (tokenBody == null || tokenBody.get("access_token") == null)
                throw new IllegalArgumentException("SOCIAL_LOGIN_FAILED");

            HttpHeaders uh = new HttpHeaders();
            uh.setBearerAuth((String) tokenBody.get("access_token"));
            ResponseEntity<Map> ures = rest.exchange(
                "https://api.line.me/v2/profile",
                HttpMethod.GET, new HttpEntity<>(uh), Map.class);
            Map<String, Object> body = ures.getBody();
            if (body == null) throw new IllegalArgumentException("SOCIAL_LOGIN_FAILED");
            return body;
        } catch (IllegalArgumentException e) { throw e; }
        catch (HttpStatusCodeException e) {
            log.error("[LINE] HTTP {} - body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalArgumentException("SOCIAL_LOGIN_FAILED");
        }
        catch (Exception e) {
            log.error("[LINE] unexpected", e);
            throw new IllegalArgumentException("SOCIAL_LOGIN_FAILED");
        }
    }
}
