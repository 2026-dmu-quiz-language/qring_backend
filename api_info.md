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
| DELETE | /api/v1/users/withdraw (Base URL 밖, 절대 경로) | 회원 탈퇴 — 사용자 데이터 전부 하드 삭제 (아래 상세) |

회원 탈퇴 (2026-09-20 추가)

DELETE /api/v1/users/withdraw   (Authorization: Bearer <accessToken>, 본문 없음)
   응답  { "success": true, "message": "탈퇴 처리가 완료되었습니다." }
   오류  USER_NOT_FOUND(400) / 토큰 없음·만료(403)
   동작  한 트랜잭션에서 아래 순서로 하드 삭제한다. 중간 실패 시 전부 롤백.
         competition_match_answer → competition_match → competition_wrong_answer → story_session
         → user_content_unlock → quiz_result → story_progress → wrong_answer → User_Study_Log
         → User_Progress → user_asset_history → User_Asset → user_language_level → users
         이후 서버 메모리의 인증 코드·재설정 토큰도 폐기. 같은 이메일로 즉시 재가입 가능.
   경로  프론트 BASE_URL 조합 차이 때문에 /api/users/withdraw, /api/v1/api/users/withdraw 도 임시로 받는다.
         프론트가 `${BASE_URL}/users/withdraw` (BASE_URL=https://q-ring.app/api/v1) 로 정리되면 두 경로는 제거 예정.
   주의  액세스 토큰은 무상태라 만료 전까지 형식상 유효하다. 탈퇴 후 /me 등은 USER_NOT_FOUND(400) 를 돌려주므로
         프론트는 탈퇴 성공 시 AsyncStorage 의 토큰·유저 정보를 지워야 한다.

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

푸시 알림 (2026-09-21 추가, 설계: PUSH_NOTIFICATION_DESIGN.md) — Base URL 밖, 절대 경로

기기 토큰 (Authorization: Bearer <accessToken>)
| Method | Path | 설명 |
|---|---|---|
| POST | /api/v1/push/token | FCM 기기 토큰 등록. 로그인 직후·토큰 갱신 시 호출. 같은 토큰은 소유자·시각만 갱신 |
| POST | /api/v1/push/token/delete | 기기 토큰 해제 (로그아웃 시). 본인 토큰만 삭제 |

1) POST /api/v1/push/token
   요청  { "token": "<fcm registration token>", "platform": "ANDROID" }   (platform 선택: ANDROID / IOS / WEB)
   응답  { "registered": true }
   오류  VALIDATION_ERROR(token 비어 있음 / 512자 초과) / 토큰 없음·만료(403)

2) POST /api/v1/push/token/delete
   요청  { "token": "<fcm registration token>" }
   응답  { "removed": true }   (본인 토큰이 아니거나 없으면 false, 오류 아님)

관리자 (ADMIN_USER_IDS 등록 계정만, 그 외 400 FORBIDDEN_ADMIN_ONLY)
| Method | Path | 설명 |
|---|---|---|
| POST | /admin/push/test | 사용자 기기 전부에 테스트 푸시 |
| POST | /admin/push/wrong-answer-reminder/run | 오답 6일차 리마인더 즉시 실행 |

3) POST /admin/push/test
   요청  { "userId": 12, "title": "(선택)", "body": "(선택)" }
   응답  { "tokens": 2, "success": 2, "failure": 0, "removedInvalidTokens": 0 }

4) POST /admin/push/wrong-answer-reminder/run?createdDate=2026-09-15   (createdDate 생략 시 오늘-6일)
   응답  { "createdDate": "2026-09-15", "targetUsers": 3, "notifiedUsers": 2, "skippedNoToken": 1,
           "sentMessages": 3, "failedMessages": 0, "removedTokens": 0 }

자동 발송  매일 20:00 Asia/Seoul (QRING_PUSH_WRONG_ANSWER_REMINDER_CRON). 대상 = 6일 전 생성돼 아직 다시 풀지 않은
           현재 학습 언어 오답이 있는 사용자 중 pushEnabled≠false 이고 기기 토큰이 있는 사용자.
           알림 data: { "type": "WRONG_ANSWER_REMINDER", "screen": "incorrect", "wrongCount": "3", "createdDate": "2026-09-15" }
설정      QRING_PUSH_ENABLED=false(기본) 면 실제 발송 없이 로그만 출력. true 면 FIREBASE_CREDENTIALS_JSON(base64/원문) 또는
           FIREBASE_CREDENTIALS_PATH 필수 (둘 다 없으면 기동 실패).
