import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { theme } from '../../constants/theme';
import { LANGUAGE_OPTIONS, LEVEL_OPTIONS } from '../../constants/setup';
import type { Language, LevelCode } from '../../api/auth';

interface Props {
  language: Language | null;
  levelCode: LevelCode | null;
  onLanguageChange: (l: Language) => void;
  onLevelChange: (l: LevelCode) => void;
  showHeader?: boolean;
}

export const LearningSetupForm = ({
  language,
  levelCode,
  onLanguageChange,
  onLevelChange,
  showHeader = true,
}: Props) => {
  return (
    <View style={styles.card}>
      {showHeader && (
        <Text style={styles.cardTitle}>🌱 나만의 맞춤 학습 설정</Text>
      )}

      <Text style={styles.label}>언어 설정</Text>
      <View style={styles.row}>
        {LANGUAGE_OPTIONS.map((opt) => {
          const active = language === opt.value;
          return (
            <TouchableOpacity
              key={opt.value}
              onPress={() => onLanguageChange(opt.value)}
              style={[
                styles.langChip,
                active ? styles.langChipActive : styles.langChipInactive,
              ]}
              activeOpacity={0.85}
            >
              <Text
                style={[
                  styles.langChipText,
                  active && styles.langChipTextActive,
                ]}
              >
                {opt.label}
              </Text>
            </TouchableOpacity>
          );
        })}
      </View>

      <Text style={[styles.label, { marginTop: 18 }]}>시작 레벨 선택</Text>
      <View style={styles.row}>
        {LEVEL_OPTIONS.map((opt) => {
          const active = levelCode === opt.value;
          return (
            <TouchableOpacity
              key={opt.value}
              onPress={() => onLevelChange(opt.value)}
              style={[
                styles.levelCard,
                active ? styles.levelCardActive : styles.levelCardInactive,
              ]}
              activeOpacity={0.85}
            >
              <Text
                style={[styles.levelLabel, active && styles.levelLabelActive]}
              >
                {opt.label}
              </Text>
              <Text
                style={[styles.levelSub, active && styles.levelSubActive]}
              >
                {opt.sub}
              </Text>
            </TouchableOpacity>
          );
        })}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#F2EFE3',
    borderRadius: 20,
    padding: 18,
    borderWidth: 1.5,
    borderColor: '#d5d5c8',
  },
  cardTitle: {
    fontSize: 14,
    fontWeight: '700',
    color: theme.colors.text,
    marginBottom: 14,
  },
  label: {
    fontSize: 12,
    fontWeight: '600',
    color: '#666',
    marginBottom: 8,
  },
  row: {
    flexDirection: 'row',
    gap: 8,
  },

  // 언어 칩 (한 줄, 균등)
  langChip: {
    flex: 1,
    paddingVertical: 10,
    borderRadius: 14,
    alignItems: 'center',
  },
  langChipActive: {
    backgroundColor: theme.colors.primary,
  },
  langChipInactive: {
    backgroundColor: theme.colors.white,
    borderWidth: 1,
    borderColor: '#d5d5c8',
  },
  langChipText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#666',
  },
  langChipTextActive: {
    color: '#fff',
  },

  // 레벨 카드 (label + sub 두 줄)
  levelCard: {
    flex: 1,
    paddingVertical: 14,
    borderRadius: 14,
    alignItems: 'center',
  },
  levelCardActive: {
    backgroundColor: theme.colors.secondary,
  },
  levelCardInactive: {
    backgroundColor: theme.colors.white,
    borderWidth: 1,
    borderColor: '#d5d5c8',
  },
  levelLabel: {
    fontSize: 13,
    fontWeight: '700',
    color: '#666',
  },
  levelLabelActive: {
    color: '#3C6933',
  },
  levelSub: {
    fontSize: 12,
    fontWeight: '600',
    color: '#999',
    marginTop: 2,
  },
  levelSubActive: {
    color: '#3C6933',
  },
});
