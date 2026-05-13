import axios from 'axios';
import { API_BASE_URL } from '../constants/config';
import { tokenStore } from './tokenStore';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
});

apiClient.interceptors.request.use(async (config) => {
  const token = await tokenStore.getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 401 응답 시 토큰 정리만 수행 (자동 재로그인은 호출부에서)
apiClient.interceptors.response.use(
  (res) => res,
  async (error) => {
    if (error.response?.status === 401) {
      await tokenStore.clear();
    }
    return Promise.reject(error);
  }
);
