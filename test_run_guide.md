> **TODO:** 임시 frontend 폴더는 제거됨. 프론트엔드팀 최신 코드를 머지한 후 아래 4-5번(프론트 실행/QR 스캔) 단계 갱신 필요.

1. MySQL 데이터베이스 생성
MySQL에서
CREATE DATABASE qring_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
2. .env 파일 루트에 넣기

3. 백엔드 실행
(루트 경로에서)
.\gradlew bootRun

   API 테스트 (Swagger UI): http://localhost:8080/swagger-ui/index.html
   - 우측 상단 Authorize 버튼에 로그인 응답의 accessToken 값을 입력하면
     인증 필요 엔드포인트도 호출 가능.

4. 프론트 실행
cd frontend
npm install        # node_modules 다운로드
npm start          # Expo Dev Server 시작
터미널에 뜨는 QR 코드를 폰의 Expo Go 앱으로 스캔.

5. 같은 네트워크의 모바일 기기로 expo go 앱을 통해 QR코드 스캔
