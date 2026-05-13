import React, { useState } from 'react';
import {
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Alert,
  ScrollView,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { ScreenWrapper } from '../../components/layout/ScreenWrapper';
import { Header } from '../../components/layout/Header';
import { theme } from '../../constants/theme';
import { AuthApi } from '../../api/auth';
import type { Language, LevelCode } from '../../api/auth';
import { LearningSetupForm } from '../../components/setup/LearningSetupForm';

export const SignUpScreen = ({ navigation }: any) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [nickname, setNickname] = useState('');
  const [storyNickname, setStoryNickname] = useState('');
  const [language, setLanguage] = useState<Language | null>(null);
  const [levelCode, setLevelCode] = useState<LevelCode | null>(null);
  const [busy, setBusy] = useState(false);

  async function handleSignUp() {
    if (!email || !password || !nickname) {
      Alert.alert('알림', '이메일/비밀번호/닉네임을 모두 입력해주세요.');
      return;
    }
    if (!language || !levelCode) {
      Alert.alert('알림', '학습 설정(언어/레벨)을 모두 선택해주세요.');
      return;
    }
    setBusy(true);
    try {
      await AuthApi.signUp({
        email,
        password,
        nickname,
        storyNickname,
        language,
        levelCode,
      });
      Alert.alert(
        '인증 코드 발송',
        '입력하신 이메일로 6자리 코드를 보냈습니다.',
        [{ text: '확인', onPress: () => navigation.navigate('EmailVerify', { email }) }]
      );
    } catch (e: any) {
      Alert.alert('회원가입 실패', e.response?.data?.message || e.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <ScreenWrapper style={{ paddingHorizontal: 0 }}>
      <Header title="회원가입" leftType="back" rightType="profile" />
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        style={{ flex: 1 }}
      >
        <ScrollView
          contentContainerStyle={styles.scroll}
          keyboardShouldPersistTaps="handled"
        >
          <Text style={styles.intro}>
            스토리룸 빠져드는 즐거운 언어 학습,{'\n'}지금 시작해 보세요!
          </Text>

          <Text style={styles.label}>아이디 (이메일)</Text>
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

          <Text style={styles.label}>비밀번호</Text>
          <TextInput
            style={styles.input}
            placeholder="비밀번호를 입력해 주세요."
            value={password}
            onChangeText={setPassword}
            secureTextEntry
            placeholderTextColor="#999"
          />

          <Text style={styles.label}>닉네임</Text>
          <TextInput
            style={styles.input}
            placeholder="2~16자, 한글/영문/숫자/_"
            value={nickname}
            onChangeText={setNickname}
            autoCapitalize="none"
            placeholderTextColor="#999"
          />

          <Text style={styles.label}>스토리 닉네임 (선택)</Text>
          <TextInput
            style={styles.input}
            placeholder="스토리에서 사용할 별명"
            value={storyNickname}
            onChangeText={setStoryNickname}
            placeholderTextColor="#999"
          />

          <LearningSetupForm
            language={language}
            levelCode={levelCode}
            onLanguageChange={setLanguage}
            onLevelChange={setLevelCode}
          />

          <TouchableOpacity
            style={[styles.primaryButton, busy && styles.buttonDisabled]}
            onPress={handleSignUp}
            disabled={busy}
          >
            <Text style={styles.primaryButtonText}>
              {busy ? '처리 중...' : '가입 완료 및 시작하기 →'}
            </Text>
          </TouchableOpacity>
        </ScrollView>
      </KeyboardAvoidingView>
    </ScreenWrapper>
  );
};

const styles = StyleSheet.create({
  scroll: {
    paddingHorizontal: 20,
    paddingTop: 8,
    paddingBottom: 40,
  },
  intro: {
    fontSize: 15,
    color: theme.colors.text,
    lineHeight: 22,
    marginBottom: 20,
  },
  label: {
    fontSize: 13,
    fontWeight: '700',
    color: theme.colors.text,
    marginTop: 14,
    marginBottom: 6,
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
    marginTop: 24,
  },
  buttonDisabled: {
    opacity: 0.6,
  },
  primaryButtonText: {
    color: '#fff',
    fontSize: 15,
    fontWeight: '700',
  },
});
