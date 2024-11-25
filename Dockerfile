# 빌드 스테이지
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

# Gradle 설정 파일만 먼저 복사
COPY build.gradle settings.gradle /app/

# 의존성만 먼저 다운받아 캐싱
RUN gradle build -x test --parallel --continue > /dev/null 2>&1 || true

# 나머지 소스 코드 복사
COPY . /app/

# 실제 애플리케이션 빌드
RUN gradle build -x test

# 실행 스테이지
FROM openjdk:21-slim
WORKDIR /app

# JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
