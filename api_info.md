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
| POST | /forgot-password | 비밀번호 찾기 1단계: 재설정 코드 발송 (LOCAL·이메일 인증 완료 계정만) |
| POST | /verify-reset-code | 비밀번호 찾기 2단계: 코드 검증 → 재설정 토큰(10분, 1회용) 발급 |
| POST | /reset-password | 비밀번호 찾기 3단계: 재설정 토큰 + 새 비밀번호 → 저장 (자동 로그인 없음) |

인증 필요 (Authorization: Bearer <accessToken>)
| Method | Path | 설명 |
|---|---|---|
| GET | /me | 내 정보 조회 |
| PUT | /preferences | 학습 설정(언어/레벨) 업데이트 — Onboarding 화면용 |

비밀번호 찾기 (2026-09-18 추가, 설계: PASSWORD_RESET_DESIGN.md)

1) POST /forgot-password
   요청  { "email": "user@example.com" }
   응답  { "message": "인증 코드를 이메일로 전송했습니다. 10분 안에 입력해 주세요.", "emailSent": true }
   오류  USER_NOT_FOUND / SOCIAL_LOGIN_ACCOUNT / EMAIL_NOT_VERIFIED / CODE_RESEND_COOLDOWN(60초) / EMAIL_SEND_FAILED

2) POST /verify-reset-code
   요청  { "email": "user@example.com", "code": "123456" }
   응답  { "resetToken": "eyJ...", "expiresInSeconds": 600 }
   오류  CODE_NOT_FOUND_OR_EXPIRED / CODE_EXPIRED / CODE_MISMATCH / TOO_MANY_ATTEMPTS(오답 5회, 코드 폐기)

3) POST /reset-password
   요청  { "resetToken": "eyJ...", "newPassword": "NewPass1!" }   (규칙: 8자 이상, 소문자·숫자 포함, 대문자 또는 특수문자 포함 — 가입과 동일)
   응답  { "success": true, "message": "비밀번호가 변경되었습니다. 새 비밀번호로 로그인해 주세요." }
   오류  INVALID_RESET_TOKEN(위조·타입 불일치) / RESET_TOKEN_EXPIRED(만료 또는 이미 사용) / VALIDATION_ERROR

참고: 가입 인증 코드(/verify-email)에도 오답 5회 폐기(TOO_MANY_ATTEMPTS)와 재발송 60초 쿨다운(CODE_RESEND_COOLDOWN)이 같이 적용된다.
