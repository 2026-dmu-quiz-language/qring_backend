API 엔드포인트
Base URL: http://localhost:8080/api/v1/auth

공개 (인증 불필요)
| Method | Path | 설명 |
|---|---|---|
| POST | /signup | 회원가입 요청 + 인증 코드 발송 (미인증 상태로 저장) |
| POST | /verify-email | 6자리 코드 검증 → 토큰 발급 |
| POST | /resend-code | 인증 코드 재발송 |
| POST | /login | 로컬 로그인 (이메일+비번) |
| POST | /logout | 로그아웃 응답 (stateless이므로 클라이언트가 토큰 삭제) |
| POST | /refresh | Refresh 토큰으로 Access 토큰 재발급 |
| POST | /oauth/google | Google ID 토큰으로 로그인 |
| POST | /oauth/kakao | Kakao authorization code로 로그인 |
| POST | /oauth/line | LINE authorization code로 로그인 |
| GET | /check-email?email=... | 이메일 사용 가능 여부 |
| GET | /check-nickname?nickname=... | 닉네임 사용 가능 여부 |

인증 필요 (Authorization: Bearer <accessToken>)
| Method | Path | 설명 |
|---|---|---|
| GET | /me | 내 정보 조회 |
| PUT | /preferences | 학습 설정(언어/레벨) 업데이트 — Onboarding 화면용 |
