import { apiClient } from './client';
import { tokenStore } from './tokenStore';

export type Language = 'JA' | 'EN' | 'ZH';
export type LevelCode = 1 | 2 | 3;

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  userId: number;
  email: string;
  nickname: string;
  authProvider: string;
  newUser: boolean;
  language: Language | null;
  levelCode: LevelCode | null;
}

export interface SignUpRequest {
  email: string;
  password: string;
  nickname: string;
  storyNickname?: string;
  language?: Language;
  levelCode?: LevelCode;
}

export interface SignUpResponse {
  userId: number;
  signupStatus: string;
  message: string;
}

export interface UpdatePreferencesRequest {
  language: Language;
  levelCode: LevelCode;
}

export const AuthApi = {
  signUp: (data: SignUpRequest) =>
    apiClient.post<SignUpResponse>('/api/v1/auth/signup', data),

  verifyEmail: (data: { email: string; code: string }) =>
    apiClient.post<AuthTokens>('/api/v1/auth/verify-email', data),

  resendCode: (email: string) =>
    apiClient.post('/api/v1/auth/resend-code', { email }),

  login: (data: { email: string; password: string }) =>
    apiClient.post<AuthTokens>('/api/v1/auth/login', data),

  refresh: (refreshToken: string) =>
    apiClient.post<AuthTokens>('/api/v1/auth/refresh', { refreshToken }),

  me: () => apiClient.get('/api/v1/auth/me'),

  googleLogin: (token: string) =>
    apiClient.post<AuthTokens>('/api/v1/auth/oauth/google', { token }),

  kakaoLogin: (token: string, redirectUri: string) =>
    apiClient.post<AuthTokens>('/api/v1/auth/oauth/kakao', { token, redirectUri }),

  updatePreferences: (data: UpdatePreferencesRequest) =>
    apiClient.put<AuthTokens>('/api/v1/auth/preferences', data),
};

export async function saveAuthTokens(tokens: AuthTokens): Promise<void> {
  await tokenStore.setAccessToken(tokens.accessToken);
  await tokenStore.setRefreshToken(tokens.refreshToken);
}

export async function logout(): Promise<void> {
  await tokenStore.clear();
}

/** 학습 설정 미완료 사용자는 OnboardingScreen으로 라우팅 */
export function needsOnboarding(tokens: AuthTokens): boolean {
  return !tokens.language || tokens.levelCode == null;
}
