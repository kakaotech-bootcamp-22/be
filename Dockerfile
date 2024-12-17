# 빌드 스테이지
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

# 의존성 캐싱을 위한 파일들만 복사
COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY gradlew .

# gradlew 실행 권한 부여
RUN chmod +x ./gradlew

# 의존성만 다운로드 (--no-daemon 옵션 추가)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사 후 빌드 (--no-daemon 옵션 추가)
COPY . .
RUN ./gradlew build -x test --no-daemon

# 실행 스테이지
FROM openjdk:21-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
