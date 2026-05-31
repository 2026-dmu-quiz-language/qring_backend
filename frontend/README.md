# Qring Frontend — 통합 가이드

이 폴더는 프론트엔드 코드가 들어올 자리입니다. 코드를 받으면 아래 절차대로 통합하면 백엔드와 한 번에 같은 q-ring.app 도메인으로 배포됩니다.

배포 방식은 reviewall 프로젝트와 동일합니다 — **빌드된 정적 파일을 nginx로 서빙**하고, nginx가 `/api` 요청만 backend 컨테이너로 프록시합니다.

---

## 1. 프론트엔드 코드 배치

이 `frontend/` 폴더 안에 프론트 소스를 배치합니다 (package.json이 `frontend/package.json`이 되도록).

Expo 프로젝트라면 일반적으로 다음 구조:
```
frontend/
├── package.json
├── app.json
├── App.tsx (또는 app/ 디렉터리)
├── src/
├── assets/
└── ...
```

## 2. Expo Web 빌드 설정

`package.json`에 web 빌드 스크립트가 있는지 확인:
```json
{
  "scripts": {
    "build": "expo export --platform web"
  }
}
```
빌드 결과물은 `frontend/dist/`에 생성됩니다 (Expo SDK 49+ 기준).

## 3. Dockerfile 생성

`frontend/Dockerfile` 내용:
```dockerfile
# Build stage
FROM node:22-alpine AS build
WORKDIR /app

# 백엔드 base URL을 빌드 시점에 주입 (필요 시)
ARG EXPO_PUBLIC_API_BASE_URL=/api
ENV EXPO_PUBLIC_API_BASE_URL=$EXPO_PUBLIC_API_BASE_URL

COPY package.json package-lock.json* ./
RUN npm install
COPY . .
RUN npm run build

# Run stage - nginx로 정적 파일 서빙 + /api 프록시
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## 4. nginx.conf 생성

`frontend/nginx.conf` 내용 — `/api/*`와 백엔드 정적 경로는 backend로, 나머지는 SPA 라우팅:
```nginx
server {
    listen 80;
    server_name q-ring.app localhost;
    root /usr/share/nginx/html;
    index index.html;

    # 백엔드 API
    location /api {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 백엔드 컨트롤러 (현재 /api prefix 없음 — 추후 정리 권장)
    location ~ ^/(contentList|chat|questionResult|dashboard) {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # Swagger / OpenAPI / Actuator (개발자용)
    location ~ ^/(swagger-ui|v3/api-docs|swagger-resources|webjars|actuator) {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
    }

    # 백엔드 정적 이미지 (썸네일 등)
    location /images {
        proxy_pass http://backend:8080;
    }

    # 나머지는 SPA로 처리 (새로고침 대응)
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

> **💡 권장**: 백엔드 컨트롤러 경로를 모두 `/api/v1/*`로 통일하면 nginx.conf가 훨씬 깔끔해집니다 (지금은 `/contentList`, `/chat` 등이 prefix 없이 떠 있어서 별도 location 필요).

## 5. .dockerignore 생성

`frontend/.dockerignore`:
```
node_modules/
dist/
.env*
.expo/
```

## 6. docker-compose.yml 활성화

루트의 `docker-compose.yml`에 frontend 서비스 주석을 풀고 cloudflared의 라우팅 변경:

```yaml
# 기존 cloudflared의 라우팅을 frontend로 변경
# cloudflared/config.yml 안에서:
#   service: http://backend:8080   →   service: http://frontend:80
```

상세는 루트 `docker-compose.yml`의 주석 블록 참고.

## 7. cloudflared/config.yml 갱신

```yaml
ingress:
  - hostname: q-ring.app
    service: http://frontend:80      # ← backend:8080에서 변경
  - service: http_status:404
```

## 8. 빌드 & 기동

```powershell
docker compose up -d --build
```

브라우저로 `https://q-ring.app` 접속하면 프론트 웹앱이, `https://q-ring.app/api/...` 호출은 백엔드가 응답합니다.

---

## 참고: 모바일 앱 (Expo Go / APK)과 동시 운영

- 모바일 앱은 같은 백엔드를 사용하지만 base URL은 `https://q-ring.app`을 그대로 호출
- nginx의 `/api/*` 프록시 덕에 모바일/웹 모두 동일한 백엔드를 공유
- CORS는 same-origin이라 신경 안 써도 됨

## 참고: 환경변수

프론트에서 백엔드 호출 base URL을 코드에서 분리하려면:
```ts
// frontend/src/config.ts
export const API_BASE_URL = process.env.EXPO_PUBLIC_API_BASE_URL || '/api';
```
도커 빌드 시 `--build-arg EXPO_PUBLIC_API_BASE_URL=/api`로 주입 가능.
