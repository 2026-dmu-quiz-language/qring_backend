# syntax=docker/dockerfile:1

# --- 1) 빌드 단계: Gradle 이미지 사용 (wrapper 다운로드 불필요) ---
FROM gradle:9.4.1-jdk21 AS builder
WORKDIR /workspace

# 의존성 캐싱: 설정만 먼저 복사
COPY build.gradle settings.gradle ./
RUN gradle --no-daemon dependencies > /dev/null 2>&1 || true

# 소스 복사 후 빌드 (테스트는 건너뜀 — 임시 실행 목적)
COPY src ./src
RUN gradle --no-daemon clean bootJar -x test

# --- 2) 런타임 단계: 슬림한 JRE 이미지 ---
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
