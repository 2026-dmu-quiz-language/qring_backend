import React, { useState } from 'react';
import {
  Text,
  TouchableOpacity,
  StyleSheet,
  Alert,
  ScrollView,
} from 'react-native';
import { ScreenWrapper } from '../../components/layout/ScreenWrapper';
import { Header } from '../../components/layout/Header';
import { theme } from '../../constants/theme';
import { AuthApi, saveAuthTokens } from '../../api/auth';
import type { Language, LevelCode } from '../../api/auth';
import { LearningSetupForm } from '../../components/setup/LearningSetupForm';

export const OnboardingScreen = ({ navigation }: any) => {
  const [language, setLanguage] = useState<Language | null>(null);
  const [levelCode, setLevelCode] = useState<LevelCode | null>(null);
  const [busy, setBusy] = useState(false);

  async function handleStart() {
    if (!language || !levelCode) {
      Alert.alert('알림', '언어와 레벨을 모두 선택해주세요.');
      return;
    }
    setBusy(true);
    try {
      const res = await AuthApi.updatePreferences({ language, levelCode });
      await saveAuthTokens(res.data);
      navigation.reset({ index: 0, routes: [{ name: 'MainTab' }] });
    } catch (e: any) {
      Alert.alert('설정 실패', e.response?.data?.message || e.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <ScreenWrapper style={{ paddingHorizontal: 0 }}>
      <Header title="초기 설정" leftType="none" rightType="profile" />
      <ScrollView
        contentContainerStyle={styles.scroll}
        keyboardShouldPersistTaps="handled"
      >
        <Text style={styles.intro}>
          환영합니다!{'\n'}어떤 언어로 썰을 풀어볼까요?
        </Text>

        <LearningSetupForm
          language={language}
          levelCode={levelCode}
          onLanguageChange={setLanguage}
          onLevelChange={setLevelCode}
        />

        <TouchableOpacity
          style={[styles.primaryButton, busy && styles.buttonDisabled]}
          onPress={handleStart}
          disabled={busy}
        >
          <Text style={styles.primaryButtonText}>
            {busy ? '저장 중...' : '설정 완료하고 시작하기 →'}
          </Text>
        </TouchableOpacity>
      </ScrollView>
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
    marginBottom: 24,
  },
  primaryButton: {
    backgroundColor: theme.colors.primary,
    borderRadius: 30,
    paddingVertical: 16,
    alignItems: 'center',
    marginTop: 32,
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
