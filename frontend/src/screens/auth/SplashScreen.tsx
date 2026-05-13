import React, { useEffect } from 'react';
import { View, Text, ActivityIndicator, StyleSheet } from 'react-native';
import { ScreenWrapper } from '../../components/layout/ScreenWrapper';
import { theme } from '../../constants/theme';
import { tokenStore } from '../../api/tokenStore';

export const SplashScreen = ({ navigation }: any) => {
  useEffect(() => {
    (async () => {
      const token = await tokenStore.getAccessToken();
      navigation.reset({
        index: 0,
        routes: [{ name: token ? 'MainTab' : 'Login' }],
      });
    })();
  }, [navigation]);

  return (
    <ScreenWrapper>
      <View style={styles.container}>
        <Text style={styles.title}>Qring</Text>
        <ActivityIndicator color={theme.colors.primary} size="large" />
      </View>
    </ScreenWrapper>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    gap: 20,
  },
  title: {
    fontSize: 36,
    fontWeight: '800',
    color: theme.colors.primary,
  },
});
