# backend-server/Dockerfile
# 빌드 스테이지
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle build -x test

# 실행 스테이지
FROM openjdk:21-slim
WORKDIR /app
# jar 파일 이름 지정
COPY --from=builder /app/build/libs/be-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
