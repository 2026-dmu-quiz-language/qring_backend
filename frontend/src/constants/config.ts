import { Platform } from 'react-native';
import Constants from 'expo-constants';

// 백엔드 API URL 자동 결정
// - Web / iOS Simulator: http://localhost:8080
// - Android Emulator: http://10.0.2.2:8080 (에뮬레이터의 localhost는 자기 자신)
// - Expo Go (실기기): Expo 개발 서버가 알려주는 PC의 LAN IP를 자동으로 사용
//
// 수동 override가 필요하면 아래 MANUAL_HOST를 'http://192.168.x.x' 같은 값으로 설정
const MANUAL_HOST: string | null = null;

function resolveBaseUrl(): string {
  if (MANUAL_HOST) return `${MANUAL_HOST}:8080`;

  // Expo Go / dev client에서 PC의 LAN IP 자동 추출
  // hostUri 예: "192.168.0.10:8081"
  const hostUri =
    Constants.expoConfig?.hostUri ??
    (Constants as any).expoGoConfig?.debuggerHost ??
    null;
  const lanIp = hostUri ? hostUri.split(':').shift() : null;

  if (lanIp && lanIp !== 'localhost' && lanIp !== '127.0.0.1') {
    return `http://${lanIp}:8080`;
  }

  // 에뮬레이터/웹 fallback
  return Platform.OS === 'android'
    ? 'http://10.0.2.2:8080'
    : 'http://localhost:8080';
}

export const API_BASE_URL = resolveBaseUrl();

// 소셜 로그인 키 (백엔드 .env와 동일하게 유지)
export const GOOGLE_WEB_CLIENT_ID =
  '472758554338-hvjco2n0okeqsfe8uv7d8ecm1ih8qlb6.apps.googleusercontent.com';

export const KAKAO_APP_KEY = '34ef02f3d1f4df166ecdfea02aa21bd1';
