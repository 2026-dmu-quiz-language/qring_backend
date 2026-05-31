# syntax=docker/dockerfile:1

# --- 1) 빌드 단계: Gradle Wrapper로 부트 jar 생성 ---
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

# 의존성 캐싱: 래퍼/설정만 먼저 복사
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x ./gradlew
RUN ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

# 소스 복사 후 빌드 (테스트는 건너뜀 — 임시 실행 목적)
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

# --- 2) 런타임 단계: 슬림한 JRE 이미지 ---
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
