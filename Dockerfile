# 빌드 스테이지
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

# 의존성 캐싱을 위한 파일들만 복사
COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY gradlew .

# 의존성만 다운로드
RUN ./gradlew dependencies

# 소스 코드 복사 후 빌드
COPY . .
RUN ./gradlew build -x test

# 실행 스테이지
FROM openjdk:21-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
