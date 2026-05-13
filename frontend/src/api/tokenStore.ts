import { Platform } from 'react-native';
import * as SecureStore from 'expo-secure-store';

const ACCESS_TOKEN_KEY = 'qring_access_token';
const REFRESH_TOKEN_KEY = 'qring_refresh_token';

const isWeb = Platform.OS === 'web';

async function get(key: string): Promise<string | null> {
  if (isWeb) {
    return typeof window !== 'undefined' ? window.localStorage.getItem(key) : null;
  }
  return SecureStore.getItemAsync(key);
}

async function set(key: string, value: string): Promise<void> {
  if (isWeb) {
    if (typeof window !== 'undefined') window.localStorage.setItem(key, value);
    return;
  }
  await SecureStore.setItemAsync(key, value);
}

async function remove(key: string): Promise<void> {
  if (isWeb) {
    if (typeof window !== 'undefined') window.localStorage.removeItem(key);
    return;
  }
  await SecureStore.deleteItemAsync(key);
}

export const tokenStore = {
  getAccessToken: () => get(ACCESS_TOKEN_KEY),
  getRefreshToken: () => get(REFRESH_TOKEN_KEY),
  setAccessToken: (token: string) => set(ACCESS_TOKEN_KEY, token),
  setRefreshToken: (token: string) => set(REFRESH_TOKEN_KEY, token),
  clear: async () => {
    await remove(ACCESS_TOKEN_KEY);
    await remove(REFRESH_TOKEN_KEY);
  },
};
