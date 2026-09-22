package com.qring.qring_backend.mypage.service;

import com.qring.qring_backend.auth.repository.UserRepository;
import com.qring.qring_backend.domain.difficulty.DifficultyLevelRepository;
import com.qring.qring_backend.domain.user.User;
import com.qring.qring_backend.domain.user.UserAssetRepository;
import com.qring.qring_backend.domain.user.UserLanguageLevel;
import com.qring.qring_backend.domain.user.UserLanguageLevelRepository;
import com.qring.qring_backend.domain.user.UserStudyLogRepository;
import com.qring.qring_backend.mypage.dto.LanguageStatusResponse;
import com.qring.qring_backend.mypage.dto.MyPageInfoResponse;
import com.qring.qring_backend.mypage.dto.MyPageLearningRequest;
import com.qring.qring_backend.mypage.dto.MyPageSwitchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 마이페이지 언어 코드: 응답은 소문자 코드(프론트 변환표 키) + 대문자 코드 + 한글명, 저장은 항상 대문자.
 * 회귀 방지 — 서버가 JA 를 주고 프론트 변환표는 ja 만 알아서 어떤 언어든 "영어"로 표시되고,
 * 그 상태로 레벨을 저장하면 EN 이 전송돼 실제 언어까지 영어로 바뀌던 버그.
 */
class MyPageServiceLanguageTest {

    private static final long USER_ID = 19L;

    private UserRepository userRepository;
    private UserLanguageLevelRepository userLanguageLevelRepository;
    private MyPageService service;
    private User user;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userLanguageLevelRepository = mock(UserLanguageLevelRepository.class);
        service = new MyPageService(userRepository, mock(UserAssetRepository.class),
                mock(DifficultyLevelRepository.class), mock(UserStudyLogRepository.class),
                userLanguageLevelRepository, mock(PasswordEncoder.class));

        user = User.builder().userId(USER_ID).nickname("Yarr").language("JA").levelCode(2).build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    }

    @Test
    @DisplayName("/mypage: 일본어 사용자는 language=ja, languageCode=JA, languageName=일본어")
    void info_returnsLowercaseCodeAndName() {
        MyPageInfoResponse res = service.getMyPageInfo(USER_ID);

        assertEquals("ja", res.getLanguage());
        assertEquals("JA", res.getLanguageCode());
        assertEquals("일본어", res.getLanguageName());
    }

    @Test
    @DisplayName("/mypage, /mypage/setting: 가입 경로와 로컬 사용자 여부를 내려준다 (LOCAL → true, 소셜 → false, 비어 있으면 LOCAL)")
    void info_and_setting_exposeAuthProvider() {
        user.setAuthProvider("LOCAL");
        assertEquals("LOCAL", service.getMyPageInfo(USER_ID).getAuthProvider());
        assertEquals(true, service.getMyPageInfo(USER_ID).getIsLocalUser());
        assertEquals(true, service.getMyPageSetting(USER_ID).getIsLocalUser());

        user.setAuthProvider("KAKAO");
        assertEquals("KAKAO", service.getMyPageSetting(USER_ID).getAuthProvider());
        assertEquals(false, service.getMyPageSetting(USER_ID).getIsLocalUser());
        assertEquals(false, service.getMyPageInfo(USER_ID).getIsLocalUser());

        user.setAuthProvider(null);   // provider 가 비어 있는 옛 row
        assertEquals(true, service.getMyPageSetting(USER_ID).getIsLocalUser());
    }

    @Test
    @DisplayName("/mypage: DB 값이 소문자·공백이어도 같은 결과, 언어 없으면 전부 null")
    void info_normalizesStoredValue() {
        user.setLanguage(" zh ");
        MyPageInfoResponse res = service.getMyPageInfo(USER_ID);
        assertEquals("zh", res.getLanguage());
        assertEquals("ZH", res.getLanguageCode());
        assertEquals("중국어", res.getLanguageName());

        user.setLanguage(null);
        res = service.getMyPageInfo(USER_ID);
        assertNull(res.getLanguage());
        assertNull(res.getLanguageCode());
        assertNull(res.getLanguageName());
    }

    @Test
    @DisplayName("/mypage/learning: 소문자 코드로 와도 대문자로 저장하고 user_language_level 도 대문자로 맞춘다")
    void updateLearning_storesUppercase() {
        when(userLanguageLevelRepository.findByUserIdAndLanguage(anyLong(), anyString())).thenReturn(Optional.empty());
        MyPageLearningRequest req = new MyPageLearningRequest();
        req.setLanguage("en");
        req.setLevelCode(3);

        service.updateLearning(USER_ID, req);

        assertEquals("EN", user.getLanguage());
        assertEquals(3, user.getLevelCode());
        ArgumentCaptor<UserLanguageLevel> saved = ArgumentCaptor.forClass(UserLanguageLevel.class);
        verify(userLanguageLevelRepository).save(saved.capture());
        assertEquals("EN", saved.getValue().getLanguage());
        assertEquals(3, saved.getValue().getLevel());
    }

    @Test
    @DisplayName("/switch: 소문자 코드로 와도 대문자로 저장하고 그 언어의 레벨을 동기화한다")
    void switchLanguage_storesUppercase() {
        unlock("ZH", 1);
        MyPageSwitchRequest req = new MyPageSwitchRequest();
        req.setLanguage("zh");

        LanguageStatusResponse res = service.switchLanguage(USER_ID, req);

        assertEquals("ZH", user.getLanguage());
        assertEquals(1, user.getLevelCode());
        verify(userRepository).save(user);
        assertEquals("ZH", res.getCurrent());
        assertEquals("중국어", res.getCurrentName());
    }

    @Test
    @DisplayName("/switch: 지원하지 않는 코드·빈 값이면 INVALID_LANGUAGE 로 거부하고 아무것도 저장하지 않는다")
    void switchLanguage_rejectsUnsupportedCode() {
        for (String bad : new String[] { null, "", "  ", "KO", "english" }) {
            MyPageSwitchRequest req = new MyPageSwitchRequest();
            req.setLanguage(bad);

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> service.switchLanguage(USER_ID, req));
            assertEquals("INVALID_LANGUAGE", e.getMessage());
        }
        assertEquals("JA", user.getLanguage());
        verify(userRepository, never()).save(user);
    }

    @Test
    @DisplayName("/switch: 해금하지 않은 언어면 LANGUAGE_NOT_UNLOCKED 로 거부한다 (레벨이 이전 언어 값으로 남는 것 방지)")
    void switchLanguage_rejectsLockedLanguage() {
        when(userLanguageLevelRepository.findByUserIdAndLanguage(USER_ID, "ZH")).thenReturn(Optional.empty());
        MyPageSwitchRequest req = new MyPageSwitchRequest();
        req.setLanguage("ZH");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.switchLanguage(USER_ID, req));

        assertEquals("LANGUAGE_NOT_UNLOCKED", e.getMessage());
        assertEquals("JA", user.getLanguage());
        assertEquals(2, user.getLevelCode());
        verify(userRepository, never()).save(user);
    }

    @Test
    @DisplayName("/langcheck: 현재 언어와 해금 목록을 함께 주고, 해금 목록은 DB 가 소문자여도 대문자로 내려간다")
    void languageStatus_returnsCurrentAndUnlocked() {
        when(userLanguageLevelRepository.findByUserId(USER_ID))
                .thenReturn(List.of(level("JA", 2), level(" zh ", 1)));

        LanguageStatusResponse res = service.getLanguageStatus(USER_ID);

        assertEquals("JA", res.getCurrent());
        assertEquals("일본어", res.getCurrentName());
        assertEquals(List.of("JA", "ZH"), res.getUnlocked());
    }

    @Test
    @DisplayName("/langcheck: 온보딩 전(언어 없음)이면 current 는 null, 해금 목록은 빈 배열")
    void languageStatus_beforeOnboarding() {
        user.setLanguage(null);
        when(userLanguageLevelRepository.findByUserId(USER_ID)).thenReturn(List.of());

        LanguageStatusResponse res = service.getLanguageStatus(USER_ID);

        assertNull(res.getCurrent());
        assertNull(res.getCurrentName());
        assertEquals(List.of(), res.getUnlocked());
    }

    /** 해당 언어를 해금 상태로 만든다 (user_language_level 에 row 가 있는 것처럼). */
    private void unlock(String language, int level) {
        when(userLanguageLevelRepository.findByUserIdAndLanguage(USER_ID, language))
                .thenReturn(Optional.of(level(language, level)));
    }

    private UserLanguageLevel level(String language, int level) {
        UserLanguageLevel ull = new UserLanguageLevel();
        ull.setUserId(USER_ID);
        ull.setLanguage(language);
        ull.setLevel(level);
        return ull;
    }
}
