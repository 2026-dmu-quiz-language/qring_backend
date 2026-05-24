# Qring Backend - File Map

프로젝트 폴더 구조와 각 파일의 역할을 한눈에 볼 수 있도록 정리한 문서.

```
qring_backend/
├─ README.md                — 프로젝트 소개
├─ api_info.md              — API 명세 정리
├─ test_run_guide.md        — 로컬 실행 가이드 (MySQL 준비, 백엔드/프론트 기동 절차)
├─ build.gradle             — Gradle 빌드 설정
├─ settings.gradle          — Gradle 멀티 프로젝트 설정
├─ gradlew / gradlew.bat    — Gradle Wrapper 실행 스크립트
├─ sql/                     — DB 초기화 SQL 스크립트 보관
├─ bin/, build/             — 빌드 산출물 (gitignore 대상)
│
└─ src/                                          — 소스 루트
   ├─ main/                                      — 운영 코드
   │  ├─ resources/                              — 설정/리소스 파일
   │  │  └─ application.yml                      — Spring 설정 (DB, JWT, OAuth, 메일 등)
   │  │
   │  └─ java/com/qring/qring_backend/           — Java 소스 루트 (베이스 패키지)
   │     │
   │     ├─ QringBackendApplication.java         — Spring Boot 진입점, 엔티티는 domain에서 스캔
   │     │
   │     ├─ config/                              — 전역 설정 (Swagger 등 인프라성 빈)
   │     │  └─ OpenApiConfig.java                — springdoc 설정, Swagger UI에 JWT Bearer 스킴 등록
   │     │
   │     ├─ auth/                                — 인증/계정 도메인 (회원가입·로그인·소셜·JWT)
   │     │  ├─ controller/                       — HTTP 엔드포인트 진입점
   │     │  │  └─ AuthController.java            — /api/v1/auth/** 라우팅
   │     │  │
   │     │  ├─ service/                          — 인증 비즈니스 로직
   │     │  │  ├─ AuthService.java               — 가입·로그인·소셜·토큰 재발급·학습 설정 핵심 로직
   │     │  │  ├─ EmailService.java              — 이메일 인증 코드 생성·발송·검증 (메모리 맵 기반)
   │     │  │  ├─ OAuthService.java              — Google/Kakao/LINE 외부 OAuth 통신 및 토큰 검증
   │     │  │  └─ DisposableEmailService.java    — 일회용/임시 이메일 도메인 차단 판별기
   │     │  │
   │     │  ├─ security/                         — Spring Security & JWT 인프라
   │     │  │  ├─ SecurityConfig.java            — 무상태 세션·CORS·인증 화이트리스트·JWT 필터 등록
   │     │  │  ├─ JwtTokenProvider.java          — JWT 액세스/리프레시 토큰 생성·검증·subject 추출
   │     │  │  └─ JwtAuthenticationFilter.java   — Bearer 토큰 검증 후 SecurityContext에 인증 주입
   │     │  │
   │     │  ├─ repository/                       — JPA 리포지토리
   │     │  │  └─ UserRepository.java            — User CRUD 및 이메일·닉네임·소셜ID 조회
   │     │  │
   │     │  ├─ dto/                              — 요청/응답 DTO
   │     │  │  ├─ AuthRequest.java               — 인증 요청 모음 (SignUp, Login, VerifyEmail, SocialLogin 등)
   │     │  │  ├─ AuthResponse.java              — 인증 성공 응답 (JWT 토큰 페어 + 사용자 정보)
   │     │  │  ├─ SignUpResponse.java            — 회원가입 1단계 응답 (가입 상태/안내 메시지)
   │     │  │  └─ UserDto.java                   — 외부 노출용 사용자 정보 DTO (비밀번호 제외)
   │     │  │
   │     │  └─ config/                           — auth 전용 설정 (예외 처리)
   │     │     └─ GlobalExceptionHandler.java    — 컨트롤러 공통 예외 처리 (도메인/검증/미처리)
   │     │
   │     ├─ dashboard/                           — 대시보드 도메인 (학습 진행 통계 집계)
   │     │  ├─ controller/
   │     │  │  └─ DashboardController.java       — /api/v1/dash 엔드포인트
   │     │  ├─ service/
   │     │  │  └─ DashboardService.java          — 진도율·연속일·완료 스토리·코멘트 집계
   │     │  └─ dto/
   │     │     └─ DashboardResponse.java         — 대시보드 응답 DTO
   │     │
   │     └─ domain/                              — 핵심 도메인 엔티티/리포지토리 (DB 모델 계층)
   │        │
   │        ├─ user/                             — 사용자 및 학습 활동 관련 엔티티
   │        │  ├─ User.java                      — 사용자 엔티티 (로컬/소셜 공용)
   │        │  ├─ UserAsset.java                 — 사용자 자산 (포인트·경험치·연속 학습일), User와 1:1
   │        │  ├─ Userprogress.java              — 사용자별 콘텐츠 진행 상태 (최근 챕터·진도율)
   │        │  ├─ UserStudyLog.java              — 퀴즈 풀이 로그 (응답·정답 여부·시각)
   │        │  ├─ UserprogressRepository.java    — 평균 진도율, 완료 스토리 수 조회
   │        │  └─ UserStudyLogRepository.java    — 학습일 목록 조회 (연속 학습일 계산용)
   │        │
   │        ├─ content/                          — 학습 콘텐츠 구조 (스토리·챕터·카테고리)
   │        │  ├─ Content.java                   — 학습 콘텐츠(스토리) (카테고리·난이도·썸네일·총 챕터 수)
   │        │  ├─ Chapter.java                   — 콘텐츠 챕터 (번호·제목·진입 필요 포인트)
   │        │  └─ ContentCategory.java           — 콘텐츠 카테고리 (표시 순서)
   │        │
   │        ├─ difficulty/                       — 난이도 등급 정의
   │        │  ├─ DifficultyLevel.java           — 난이도 등급 (코드·표시명·설명)
   │        │  └─ DifficultyLevelRepository.java — DifficultyLevel CRUD
   │        │
   │        ├─ goal/                             — 일일 학습 목표 기록
   │        │  └─ DailyGoalRecord.java           — 일일 목표 달성 기록 (사용자·챕터·날짜·달성 여부)
   │        │
   │        ├─ quiz/                             — 퀴즈 및 채점 (점수 산정 테이블·성취 코멘트 포함)
   │        │  ├─ QuizDetail.java                — 스크립트 연동 퀴즈 (유형·정답·해설)
   │        │  ├─ QuizResult.java                — 사용자별 퀴즈 풀이 결과 (난이도·시도·힌트·점수)
   │        │  ├─ QuizResultRepository.java      — 사용자 누적 점수 조회
   │        │  ├─ ScoreTable.java                — 점수 산정 테이블 (난이도·시도·힌트 조합 → 점수)
   │        │  ├─ ScoreTableId.java              — ScoreTable 복합키
   │        │  ├─ ScoreTableRepository.java      — 복합키 조건으로 점수 조회
   │        │  ├─ AchievementComment.java        — 진도율 구간별 성취 코멘트
   │        │  ├─ AchievementCommentRepository.java — 진도율로 코멘트 조회
   │        │  └─ QuizService.java               — (현재 빈 파일)
   │        │
   │        └─ script/                           — 스토리 스크립트 (대사·선택지)
   │           ├─ Script.java                    — 챕터 내 대사 한 줄, 선택지(옵션) 연결
   │           └─ ScriptOption.java              — 스크립트 선택지 (다음 스크립트·엔딩 점수 가중치)
   │
   └─ test/java/com/qring/qring_backend/         — 테스트 코드
      └─ QringBackendApplicationTests.java       — Spring Boot 컨텍스트 로드 테스트
```

## 폴더 역할 요약

| 폴더 | 역할 |
|------|------|
| `config/` | 애플리케이션 전역 설정 (Swagger 등 인프라성 빈) |
| `auth/` | 인증·계정 도메인. 로컬/소셜 가입, 로그인, JWT 발급 전부 담당 |
| `auth/controller/` | HTTP 엔드포인트 진입점 |
| `auth/service/` | 인증 비즈니스 로직 (이메일 인증, OAuth 검증 등 보조 서비스 포함) |
| `auth/security/` | Spring Security 설정 + JWT 토큰 인프라 |
| `auth/repository/` | JPA 리포지토리 (User 조회) |
| `auth/dto/` | 인증 관련 요청/응답 DTO |
| `auth/config/` | auth 한정 설정 — 공통 예외 처리 |
| `dashboard/` | 학습 진행 통계 집계 및 응답 |
| `domain/` | 핵심 DB 모델 계층. 엔티티와 그에 직결된 리포지토리만 둠 |
| `domain/user/` | 사용자 정보·자산·진도·학습 로그 |
| `domain/content/` | 학습 콘텐츠(스토리)·챕터·카테고리 구조 |
| `domain/difficulty/` | 난이도 등급 마스터 |
| `domain/goal/` | 일일 학습 목표 달성 이력 |
| `domain/quiz/` | 퀴즈 정의·풀이 결과·점수 산정 테이블·성취 코멘트 |
| `domain/script/` | 스토리 스크립트 한 줄과 선택지 |
