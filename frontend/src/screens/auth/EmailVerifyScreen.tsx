import React, { useState } from 'react';
import {
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Alert,
  View,
} from 'react-native';
import { ScreenWrapper } from '../../components/layout/ScreenWrapper';
import { Header } from '../../components/layout/Header';
import { theme } from '../../constants/theme';
import { AuthApi, saveAuthTokens, needsOnboarding } from '../../api/auth';

export const EmailVerifyScreen = ({ route, navigation }: any) => {
  const email: string = route.params?.email ?? '';
  const [code, setCode] = useState('');
  const [busy, setBusy] = useState(false);

  async function handleVerify() {
    if (code.length !== 6) {
      Alert.alert('알림', '6자리 코드를 입력해주세요.');
      return;
    }
    setBusy(true);
    try {
      const res = await AuthApi.verifyEmail({ email, code });
      await saveAuthTokens(res.data);
      const next = needsOnboarding(res.data) ? 'Onboarding' : 'MainTab';
      navigation.reset({ index: 0, routes: [{ name: next }] });
    } catch (e: any) {
      Alert.alert('인증 실패', e.response?.data?.message || e.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleResend() {
    try {
      await AuthApi.resendCode(email);
      Alert.alert('재발송 완료', '인증 코드를 다시 보냈습니다.');
    } catch (e: any) {
      Alert.alert('재발송 실패', e.response?.data?.message || e.message);
    }
  }

  return (
    <ScreenWrapper style={{ paddingHorizontal: 0 }}>
      <Header title="이메일 인증" leftType="back" rightType="none" />
      <View style={styles.container}>
        <Text style={styles.guide}>
          {email}{'\n'}로 보낸 6자리 코드를 입력해주세요.
        </Text>
        <TextInput
          style={styles.input}
          placeholder="000000"
          value={code}
          onChangeText={setCode}
          keyboardType="number-pad"
          maxLength={6}
          textAlign="center"
          placeholderTextColor="#bbb"
        />
        <TouchableOpacity
          style={[styles.primaryButton, busy && styles.buttonDisabled]}
          onPress={handleVerify}
          disabled={busy}
        >
          <Text style={styles.primaryButtonText}>
            {busy ? '확인 중...' : '확인'}
          </Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={handleResend} style={{ marginTop: 12 }}>
          <Text style={styles.linkText}>코드 재발송</Text>
        </TouchableOpacity>
      </View>
    </ScreenWrapper>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingHorizontal: 20,
    paddingTop: 32,
  },
  guide: {
    fontSize: 14,
    color: theme.colors.text,
    textAlign: 'center',
    marginBottom: 32,
    lineHeight: 22,
  },
  input: {
    backgroundColor: theme.colors.white,
    borderWidth: 1.5,
    borderColor: '#d5d5c8',
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 18,
    fontSize: 24,
    fontWeight: '700',
    letterSpacing: 8,
    color: theme.colors.text,
  },
  primaryButton: {
    backgroundColor: theme.colors.primary,
    borderRadius: 12,
    paddingVertical: 16,
    alignItems: 'center',
    marginTop: 24,
  },
  buttonDisabled: {
    opacity: 0.6,
  },
  primaryButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '700',
  },
  linkText: {
    color: theme.colors.primary,
    fontSize: 13,
    textAlign: 'center',
    fontWeight: '600',
  },
});
