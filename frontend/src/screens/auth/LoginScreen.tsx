import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Alert,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import * as Google from 'expo-auth-session/providers/google';
import * as AuthSession from 'expo-auth-session';
import * as WebBrowser from 'expo-web-browser';
import { ScreenWrapper } from '../../components/layout/ScreenWrapper';
import { theme } from '../../constants/theme';
import { AuthApi, saveAuthTokens, needsOnboarding } from '../../api/auth';
import type { AuthTokens } from '../../api/auth';
import { GOOGLE_WEB_CLIENT_ID, KAKAO_APP_KEY } from '../../constants/config';

WebBrowser.maybeCompleteAuthSession();

const KAKAO_DISCOVERY = {
  authorizationEndpoint: 'https://kauth.kakao.com/oauth/authorize',
  tokenEndpoint: 'https://kauth.kakao.com/oauth/token',
};

export const LoginScreen = ({ navigation }: any) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [busy, setBusy] = useState(false);

  // ─── Google ───
  const [, googleResponse, promptGoogle] = Google.useAuthRequest({
    clientId: GOOGLE_WEB_CLIENT_ID,
    webClientId: GOOGLE_WEB_CLIENT_ID,
  });

  useEffect(() => {
    if (googleResponse?.type === 'success') {
      const idToken =
        (googleResponse.authentication as any)?.idToken ||
        (googleResponse.params as any)?.id_token;
      if (idToken) handleSocial(() => AuthApi.googleLogin(idToken), 'Google');
    }
  }, [googleResponse]);

  // ─── Kakao ───
  const kakaoRedirectUri = AuthSession.makeRedirectUri();
  const [, kakaoResponse, promptKakao] = AuthSession.useAuthRequest(
    {
      clientId: KAKAO_APP_KEY,
      redirectUri: kakaoRedirectUri,
      responseType: AuthSession.ResponseType.Code,
    },
    KAKAO_DISCOVERY
  );

  useEffect(() => {
    if (kakaoResponse?.type === 'success') {
      const code = kakaoResponse.params.code;
      if (code) {
        handleSocial(() => AuthApi.kakaoLogin(code, kakaoRedirectUri), 'Kakao');
      }
    }
  }, [kakaoResponse]);

  // ─── Handlers ───
  function routeAfterAuth(tokens: AuthTokens) {
    const next = needsOnboarding(tokens) ? 'Onboarding' : 'MainTab';
    navigation.reset({ index: 0, routes: [{ name: next }] });
  }

  async function handleLogin() {
    if (!email || !password) {
      Alert.alert('알림', '이메일과 비밀번호를 모두 입력해주세요.');
      return;
    }
    setBusy(true);
    try {
      const res = await AuthApi.login({ email, password });
      await saveAuthTokens(res.data);
      routeAfterAuth(res.data);
    } catch (e: any) {
      Alert.alert('로그인 실패', e.response?.data?.message || e.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleSocial(call: () => Promise<any>, label: string) {
    setBusy(true);
    try {
      const res = await call();
      await saveAuthTokens(res.data);
      routeAfterAuth(res.data);
    } catch (e: any) {
      Alert.alert(`${label} 로그인 실패`, e.response?.data?.message || e.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <ScreenWrapper>
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        style={styles.container}
      >
        <Text style={styles.appTitle}>Qring</Text>
        <Text style={styles.welcome}>환영합니다!</Text>
        <Text style={styles.subtitle}>오늘의 학습을 시작할 준비가 되셨나요?</Text>

        <Text style={styles.label}>ID</Text>
        <TextInput
          style={styles.input}
          placeholder="아이디를 입력해 주세요."
          value={email}
          onChangeText={setEmail}
          keyboardType="email-address"
          autoCapitalize="none"
          autoCorrect={false}
          placeholderTextColor="#999"
        />

        <Text style={styles.label}>PASSWORD</Text>
        <TextInput
          style={styles.input}
          placeholder="비밀번호를 입력해 주세요."
          value={password}
          onChangeText={setPassword}
          secureTextEntry
          placeholderTextColor="#999"
        />

        <TouchableOpacity
          style={[styles.primaryButton, busy && styles.buttonDisabled]}
          onPress={handleLogin}
          disabled={busy}
        >
          <Text style={styles.primaryButtonText}>
            {busy ? '로그인 중...' : '로그인 →'}
          </Text>
        </TouchableOpacity>

        <Text style={styles.helpText}>비밀번호를 잊으셨나요?</Text>

        <View style={styles.divider}>
          <View style={styles.dividerLine} />
          <Text style={styles.dividerText}>OR</Text>
          <View style={styles.dividerLine} />
        </View>

        <View style={styles.socialRow}>
          <TouchableOpacity
            style={styles.socialIcon}
            onPress={() => promptGoogle()}
            disabled={busy}
          >
            <Text style={styles.socialIconText}>G</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.socialIcon, styles.kakaoIcon]}
            onPress={() => promptKakao()}
            disabled={busy}
          >
            <Text style={styles.socialIconText}>K</Text>
          </TouchableOpacity>
        </View>

        <TouchableOpacity onPress={() => navigation.navigate('SignUp')} style={{ marginTop: 16 }}>
          <Text style={styles.linkText}>
            계정이 없으신가요? <Text style={styles.linkBold}>회원가입</Text>
          </Text>
        </TouchableOpacity>
      </KeyboardAvoidingView>
    </ScreenWrapper>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
  },
  appTitle: {
    fontSize: 36,
    fontWeight: '800',
    color: theme.colors.primary,
    textAlign: 'center',
  },
  welcome: {
    fontSize: 22,
    fontWeight: '800',
    color: theme.colors.text,
    textAlign: 'center',
    marginTop: 12,
  },
  subtitle: {
    fontSize: 13,
    color: '#888',
    textAlign: 'center',
    marginTop: 4,
    marginBottom: 24,
  },
  label: {
    fontSize: 11,
    fontWeight: '700',
    color: '#666',
    marginTop: 12,
    marginBottom: 6,
    letterSpacing: 0.5,
  },
  input: {
    backgroundColor: theme.colors.white,
    borderWidth: 1.5,
    borderColor: '#d5d5c8',
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 14,
    fontSize: 15,
    color: theme.colors.text,
  },
  primaryButton: {
    backgroundColor: theme.colors.primary,
    borderRadius: 30,
    paddingVertical: 16,
    alignItems: 'center',
    marginTop: 20,
  },
  buttonDisabled: {
    opacity: 0.6,
  },
  primaryButtonText: {
    color: '#fff',
    fontSize: 15,
    fontWeight: '700',
  },
  helpText: {
    fontSize: 12,
    color: '#888',
    textAlign: 'center',
    marginTop: 14,
  },
  divider: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 20,
    marginBottom: 16,
  },
  dividerLine: {
    flex: 1,
    height: 1,
    backgroundColor: '#d5d5c8',
  },
  dividerText: {
    marginHorizontal: 12,
    color: '#999',
    fontSize: 12,
  },
  socialRow: {
    flexDirection: 'row',
    justifyContent: 'center',
    gap: 16,
  },
  socialIcon: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: theme.colors.white,
    borderWidth: 1.5,
    borderColor: '#d5d5c8',
    alignItems: 'center',
    justifyContent: 'center',
  },
  kakaoIcon: {
    backgroundColor: '#FEE500',
    borderColor: '#FEE500',
  },
  socialIconText: {
    fontSize: 16,
    fontWeight: '800',
    color: '#222',
  },
  linkText: {
    fontSize: 13,
    color: '#666',
    textAlign: 'center',
  },
  linkBold: {
    color: theme.colors.primary,
    fontWeight: '700',
  },
});
