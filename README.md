# 가짜 리뷰 검사 서비스 (BE)
이 프로젝트는 네이버 블로그 게시글의 가짜 리뷰 여부를 판별하는 서비스의 백엔드입니다. 프론트엔드에서 요청한 URL을 AI 모델에 전달하고, 결과를 반환합니다. 또한 사용자가 남긴 피드백을 관리합니다.

<img width="1691" alt="스크린샷 2024-10-31 오후 1 10 26" src="https://github.com/user-attachments/assets/0fc9e22a-b799-44b9-b92f-7366ba46fd90">




## 프로젝트 구조

- **프레임워크**: Java Spring Boot
- **데이터베이스**: PostgreSQL
- **주요 기능**:
  - 가짜 리뷰 검사: 블로그 URL을 분석하여 가짜 리뷰 여부를 판단
  - 피드백 관리: 검사 결과에 대한 사용자 피드백 수집 및 저장
- **API 문서화**: Swagger 사용

## 설치 및 실행

1. **의존성 설치**:
   ```bash
   ./gradlew build
   ```
2. **개발 서버 실행**:
   ```bash
   ./gradlew bootRun
   ```
개발 서버는 기본적으로 `http://localhost:8080`에서 실행됩니다.

## 환경 변수 설정
이 프로젝트는 AWS Parameter Store를 통해 환경 변수를 관리합니다. 필요 변수는 AWS 콘솔의 Parameter Store에 설정해야 합니다. 필요한 변수 목록은 다음과 같습니다:

- `DB_URL`: PostgreSQL 데이터베이스 URL
- `DB_USERNAME`: PostgreSQL 사용자명
- `DB_PASSWORD`: PostgreSQL 비밀번호
- `JWT_SECRET`: JWT 토큰 서명용 비밀키
- `OAUTH_KAKAO_CLIENT_ID`: Kakao 소셜 로그인 클라이언트 ID
- `OAUTH_GOOGLE_CLIENT_ID`: Google 소셜 로그인 클라이언트 ID

## 주요 기능
- 가짜 리뷰 검사 API: 입력된 블로그 URL을 AI 서버에 전달하고, 분석 결과를 반환합니다.
- 피드백 API: 사용자 피드백을 수집하고 저장합니다.
- OAuth2 소셜 로그인: Kakao와 Google을 통한 소셜 로그인 구현

## Swagger 문서 확인
API 문서는 Swagger를 통해 제공합니다. 애플리케이션을 실행한 후 다음 주소에서 확인할 수 있습니다:
```bash
http://localhost:8080/swagger-ui/index.html
```
