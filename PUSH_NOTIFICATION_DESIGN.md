# 푸시 알림 (FCM) 설계 — 오답 6일차 리마인더

작성일: 2026-09-21 · 담당: 백엔드(임태형) · 상태: 구현 완료, 실기기 검증 대기

## 1. 배경

대시보드의 `incorrectAlarm`("오답을 확인한 지 7일이 지났어요!")은 API 응답에 실리는 **인앱 문구**라
사용자가 앱을 열어야만 보인다. 오답(wrong_answer)은 다시 풀면 지워지고 **7일이 지나면 오답 노트에서 사라지는데**,
그 전에 앱 밖에서 사용자를 다시 불러들일 수단이 없었다. users.push_enabled 토글은 있었지만 읽는 코드가 없었다.

이 문서는 서버가 기기로 직접 보내는 **OS 푸시 알림**을 추가한 설계다. 첫 사용처는 **오답 생성 6일차 리마인더** 하나.

## 2. 결정 사항

| # | 항목 | 결정 | 이유 |
|---|---|---|---|
| 1 | 발송 채널 | Firebase Cloud Messaging (firebase-admin 9.4.3) | Android·iOS(APNs 래핑) 한 SDK로 처리. RN 진영 표준 |
| 2 | 토큰 저장 | 별도 테이블 `user_device_token` (1 사용자 : N 기기) | users 컬럼 하나로는 다기기·계정 전환을 못 다룸 |
| 3 | 토큰 유일성 | `token` UNIQUE. 다른 계정으로 로그인하면 소유자 재배정 | 한 기기 = 한 토큰. 이전 계정으로 새지 않게 |
| 4 | 발송 시점 | 오답 **생성일 + 6일**, 매일 20:00 Asia/Seoul 1회 | 7일이 되면 노트에서 사라지므로 하루 전 저녁에 마지막 안내 |
| 5 | 대상 판정 | 그 날 생성돼 **아직 남아 있는** 오답이 있고, 현재 학습 언어이며, push_enabled≠false | 대시보드 오답 알람과 같은 스코프. 다시 풀어 지워진 오답은 자동 제외 |
| 6 | 문구 | 제목 "오답 노트가 기다리고 있어요 📝" / 본문 "6일 전에 틀린 문제 N개가 아직 남아 있어요. 내일이면 오답 노트에서 사라지니 지금 복습해 보세요!" | 개수를 넣어 구체적으로. 사라진다는 마감 압박 |
| 7 | data 페이로드 | `type=WRONG_ANSWER_REMINDER`, `screen=incorrect`, `wrongCount`, `createdDate` | 앱이 탭 시 오답 노트로 라우팅. `screen` 값은 프론트와 확정 필요 |
| 8 | 무효 토큰 | FCM 응답 UNREGISTERED / INVALID_ARGUMENT → 즉시 삭제 | 앱 삭제·토큰 회전 기기에 계속 보내지 않게. 일시 오류는 유지 |
| 9 | 끄기 | `qring.push.enabled=false`(기본) 면 LogPushSender 가 로그만 출력 | 로컬·CI 에서 Firebase 키 없이 기동. 배포 환경만 true |
| 10 | 관리자 API | 테스트 푸시 · 리마인더 즉시 실행 (ADMIN_USER_IDS) | 스케줄 시각을 기다리지 않고 실기기 검증 |
| 11 | 인앱 문구 | `incorrectAlarm` 은 그대로 유지 | 푸시는 앱 밖에서 불러들이는 용도, 인앱은 들어온 뒤 화면 안내 — 역할이 다름 |
| 12 | 탈퇴 | UserWithdrawalService 에서 토큰도 삭제 | 탈퇴한 기기로 발송되지 않게 |

## 3. 구성 요소

```
config/FirebaseConfig.java                 FirebaseApp·FirebaseMessaging 빈 (enabled=true 일 때만)
config/SchedulingConfig.java               @EnableScheduling
domain/user/UserDeviceToken(.Repository)   기기 토큰 엔티티·조회·정리
domain/quiz/WrongAnswerReminderTarget      대상 프로젝션 (userId, wrongCount)
domain/quiz/WrongAnswerRepository          findReminderTargetsCreatedBetween(start, end)
push/service/PushSender                    발송 인터페이스
push/service/FcmPushSender                 FCM multicast (500개 단위), 무효 토큰 분류
push/service/LogPushSender                 enabled=false 대체 구현 (로그만)
push/service/PushTokenService              토큰 upsert·해제·묶음 조회·무효 삭제
push/service/WrongAnswerReminderService    대상 조회 → 사용자별 발송 → 무효 토큰 정리 → 결과
push/scheduler/WrongAnswerReminderScheduler  cron 진입점 (예외는 로그로)
push/controller/PushTokenController        POST /api/v1/push/token, /token/delete
push/controller/PushAdminController        POST /admin/push/test, /admin/push/wrong-answer-reminder/run
```

## 4. 흐름

### 4-1. 토큰 등록 (앱)
1. 로그인 성공 직후, 그리고 FCM `onTokenRefresh` 때 `POST /api/v1/push/token {token, platform}`.
2. 서버는 token 으로 찾아 있으면 소유자·platform·last_seen_at 갱신, 없으면 생성.
3. 로그아웃 시 `POST /api/v1/push/token/delete {token}` — 본인 토큰만 삭제.

### 4-2. 오답 6일차 리마인더 (서버, 매일 20:00 KST)
1. `createdDate = 오늘 − 6일`, 창 = `[createdDate 00:00, +1일 00:00)`.
2. 한 쿼리로 대상 `(userId, wrongCount)` 집계 — wrong_answer ⋈ quiz_content(lang_code = users.language) ⋈ users(push_enabled≠false).
3. 대상 userId 들의 토큰을 IN 조회 한 번으로 가져와 사용자별로 묶는다.
4. 사용자마다 문구를 만들어 기기 토큰 전부에 multicast. 토큰이 없는 사용자는 `skippedNoToken`.
5. UNREGISTERED / INVALID_ARGUMENT 토큰을 모아 한 번에 삭제.
6. 결과(`WrongAnswerReminderRunResult`)를 로그로 남긴다. 관리자 API 로 실행하면 응답으로도 준다.

트랜잭션은 걸지 않는다. 발송은 외부 네트워크라 DB 커넥션을 물고 있을 이유가 없고, 각 리포지토리 호출이 자체 트랜잭션이다.

### 4-3. 시간대
- 대상 창은 **JVM 기본 시간대의 LocalDateTime** 으로 만든다. `wrong_answer.created_at` 이 `@CreationTimestamp` 로 같은 시간대에 기록되기 때문 (Dockerfile 이 Asia/Seoul 고정, QRING_FEATURE_TEST_CASES #141).
- cron 은 `zone = "Asia/Seoul"` 로 명시해 컨테이너 TZ 와 무관하게 20:00 KST 에 돈다.

## 5. 설정 (application.yml → 환경변수)

| 키 | 환경변수 | 기본 | 설명 |
|---|---|---|---|
| qring.push.enabled | QRING_PUSH_ENABLED | false | true 면 Firebase 초기화 + 실제 발송 |
| qring.push.firebase.credentials-path | FIREBASE_CREDENTIALS_PATH | (빈) | 서비스 계정 키 json 파일 경로 |
| qring.push.firebase.credentials-json | FIREBASE_CREDENTIALS_JSON | (빈) | 키 json 원문 또는 base64 (컨테이너 시크릿용) |
| qring.push.wrong-answer-reminder.enabled | QRING_PUSH_WRONG_ANSWER_REMINDER_ENABLED | true | 스케줄러 on/off |
| qring.push.wrong-answer-reminder.cron | QRING_PUSH_WRONG_ANSWER_REMINDER_CRON | `0 0 20 * * *` | 발송 시각 (Asia/Seoul) |
| qring.push.wrong-answer-reminder.days-after | — | 6 | 생성 N일차 |

enabled=true 인데 키가 둘 다 비어 있으면 **기동 실패** (켜 놓고 조용히 안 나가는 상황 방지).

## 6. 배포 전 준비 (팀)

1. Firebase 프로젝트 생성 → Android 앱 등록(`google-services.json` 은 프론트), iOS 는 APNs 키 업로드.
2. 프로젝트 설정 → 서비스 계정 → 새 비공개 키 생성 (json). **저장소에 커밋 금지.**
3. 배포 환경에 `QRING_PUSH_ENABLED=true`, `FIREBASE_CREDENTIALS_JSON=<base64>` (또는 파일 마운트 + PATH).
4. `ddl-auto: update` 로 `user_device_token` 테이블은 첫 기동 때 자동 생성된다.
5. `ADMIN_USER_IDS` 에 검증할 계정 userId 등록 후 §7 로 실기기 확인.

## 7. 실기기 검증 절차

1. 앱에서 로그인 → `POST /api/v1/push/token` 이 200 `{registered:true}` 인지, DB `user_device_token` 에 row 생겼는지.
2. 관리자 계정으로 `POST /admin/push/test {"userId": <내 id>}` → 기기에 "Qring 테스트 알림" 도착. 응답 `success` ≥ 1.
3. 오답을 하나 만든 뒤 DB 에서 그 row 의 `created_at` 을 6일 전으로 바꾸고 `POST /admin/push/wrong-answer-reminder/run` → 리마인더 도착, 응답 `notifiedUsers=1`.
   (또는 `?createdDate=YYYY-MM-DD` 로 실제 생성일을 지정)
4. 오답을 다시 풀어 지운 뒤 같은 실행 → `targetUsers=0`.
5. 마이페이지에서 푸시 끄기(pushEnabled=false) 후 실행 → 대상 제외.
6. 앱 삭제 후 실행 → 응답 `removedTokens=1`, DB 에서 토큰 사라짐.
7. 로그아웃 → `POST /api/v1/push/token/delete` 200 `{removed:true}`.

## 8. 프론트 연동 요구 (백엔드 → 프론트 전달용)

- 로그인 직후·토큰 갱신 시 `POST /api/v1/push/token` `{ "token": "<fcm>", "platform": "ANDROID" | "IOS" }` (Bearer).
- 로그아웃 시 `POST /api/v1/push/token/delete` `{ "token": "<fcm>" }`.
- 알림 탭 핸들러: `data.type === "WRONG_ANSWER_REMINDER"` → 오답 노트 화면. `data.screen` 값(`incorrect`)은 프론트 라우트명에 맞춰 바꿔 줄 수 있다.
- Android 13+ 는 POST_NOTIFICATIONS 권한 요청 필요. 알림 채널은 기본 채널 사용 (서버는 channel_id 를 지정하지 않음).

## 9. 남은 결정 / 확장 여지

1. `days-after` 6 확정 여부 (5일차·7일차 인앱 문구와의 관계).
2. 발송 시각 20:00 KST 확정 여부.
3. `data.screen` 값 프론트 라우트명과 맞추기.
4. 오답 외 다른 리마인더(연속 학습 끊김 전날 등)는 같은 `PushSender`·스케줄러 패턴으로 추가 가능.
5. 여러 인스턴스로 확장하면 스케줄러는 한 인스턴스만 `QRING_PUSH_WRONG_ANSWER_REMINDER_ENABLED=true` 로 둔다 (분산 락 없음).

## 10. 테스트

- `push/service/WrongAnswerReminderServiceTest` — 창 계산, 사용자별 1회 발송·문구, 토큰 없음 건너뛰기, 무효 토큰 정리, 대상 없음, runForToday.
- `push/service/PushTokenServiceTest` — 신규 등록·platform 정규화, 소유자 재배정, 본인 토큰만 해제, 묶음 조회, 빈 목록 no-op.
- `auth/service/UserWithdrawalServiceTest` — 탈퇴 시 토큰 삭제 포함.
- 실제 FCM 발송은 §7 로 실기기에서 확인 (자동 테스트 없음).
