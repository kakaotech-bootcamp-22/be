package com.spring.be.jwt.service;

import com.spring.be.entity.User;
import com.spring.be.jwt.config.JwtUtils;
import com.spring.be.jwt.dto.GoogleAuthResponseDto;
import com.spring.be.jwt.dto.KakaoAuthResponseDto;
import com.spring.be.jwt.dto.UserLoginStatusDto;
import com.spring.be.user.dto.UserResponseDto;
import com.spring.be.user.service.UserService;
import com.spring.be.util.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final List<String> allowedRedirectUris;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    private final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
    private final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";



    @Value("${REACT_APP_KAKAO_JS_KEY:default-value}")
    private String kakaoJsKey;

    @Value("${REACT_APP_GOOGLE_CLIENT_ID:default-value}")
    private String googleClientId;

    @Value("${spring.redirectUri}")
    private String redirectUri;

    @Value("${spring.redirectUriOrigin}")
    private String redirectUriOrigin;

    @Value("${spring.apiServerOrigin}")
    private String apiServerOrigin;

    @Autowired
    public AuthService(@Value("${spring.redirectUri}") String allowedRedirectUrisString,
                       JwtUtils jwtUtils,
                       UserService userService) {
        this.allowedRedirectUris = List.of(allowedRedirectUrisString.split(","));
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    public String generateToken(Long kakaoUserId) {
        String userId = String.valueOf(kakaoUserId);
        return jwtUtils.generateJwtToken(userId);
    }
    public String generateToken(BigInteger kakaoUserId) {
        String userId = String.valueOf(kakaoUserId);
        return jwtUtils.generateJwtToken(userId);
    }

    public ResponseEntity<?> checkLoginStatus(String token) {
        if (token == null || !jwtUtils.validateJwtToken(token)) {
            return ResponseEntity.ok().body(Map.of("isLoggedIn", false, "message", "Invalid or expired token."));
        }

        String socialIdString = jwtUtils.getUsernameFromJwtToken(token);
        BigInteger socialId = new BigInteger(socialIdString);

        User user = userService.findBySocialId(socialId);
        if (user == null) {
            return ResponseEntity.ok().body(Map.of("isLoggedIn", false, "message", "User not found."));
        }

        return ResponseEntity.ok().body(new UserLoginStatusDto(
                true, "User is logged in.", user.getNickname(), user.getUserImage(), user.getSocialPlatform(), user.getCreatedAt(), user.getEmail()
        ));
    }

    public ResponseEntity<?> performKakaoLogout(String token) {
        // 쿠키에서 JWT 추출
        if (token == null || !jwtUtils.validateJwtToken(token)) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid or expired token."));
        }

        // JWT에서 socialId 추출
        String socialIdString = jwtUtils.getUsernameFromJwtToken(token);
        BigInteger socialId = new BigInteger(socialIdString);

        // 데이터베이스에서 사용자 정보 조회
        User user = userService.findBySocialId(socialId);
        if (user == null || user.getAccessToken() == null) {
            return ResponseEntity.badRequest().body("User not found or access token is missing.");
        }

        // 카카오 로그아웃 API 호출
        String kakaoAccessToken = user.getAccessToken();
        String kakaoLogoutUrl = "https://kapi.kakao.com/v1/user/logout";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + kakaoAccessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.postForEntity(kakaoLogoutUrl, entity, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            // 로그아웃 성공시 사용자 세션 및 쿠키 해제
            ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", "")
                    .httpOnly(false)// 테스트용 - false
                    .secure(true) // 필요에 따라 설정
                    .path("/")
                    .maxAge(0)
                    .sameSite("Strict")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body("Kakao logout successful. Response: " + response.getBody());
        } else {
            return ResponseEntity.status(response.getStatusCode())
                    .body("Failed to log out from Kakao. Response: " + response.getBody());
        }
    }

    public ResponseEntity<?> performGoogleLogout(String token) {
        if (token == null || !jwtUtils.validateJwtToken(token)) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid or expired token."));
        }

        // JWT에서 socialId 추출
        String socialIdString = jwtUtils.getUsernameFromJwtToken(token);
        BigInteger socialId = new BigInteger(socialIdString);

        // 데이터베이스에서 사용자 정보 조회
        User user = userService.findBySocialId(socialId);
        if (user == null || user.getAccessToken() == null) {
            return ResponseEntity.badRequest().body("User not found or access token is missing.");
        }

        // 구글 로그아웃 API 호출
        String googleToken = user.getAccessToken();
        String googleLogoutUrl = "https://oauth2.googleapis.com/revoke";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 요청 본문에 토큰을 포함하여 전송
        String body = "token=" + googleToken;
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(googleLogoutUrl, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            // 토큰 해제 성공
            ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", "")
                    .httpOnly(false)// 테스트용 - false
                    .secure(true) // 필요에 따라 설정
                    .path("/")
                    .maxAge(0)
                    .sameSite("Strict")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body("Google logout successful.Response: " + response.getBody());
        } else {
            // 실패 처리
            return ResponseEntity.status(response.getStatusCode()).body("Failed to revoke Google token");
        }
    }

    public GoogleAuthResponseDto handleGoogleAuthentication(String googleAccessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + googleAccessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                GOOGLE_USER_INFO_URL,
                HttpMethod.GET,
                entity,
                Map.class
        );

        // 사용자 정보 가져오기
        Map<String, Object> userInfo = response.getBody();
        String googleUserId = (String) userInfo.get("sub");
        String name = (String) userInfo.get("name");
        String email = (String) userInfo.get("email");
        String profileImage = (String) userInfo.get("picture");
        BigInteger bigGoogleUserId = new BigInteger(googleUserId);
        String platform = "google";

        // 사용자 정보 저장
        userService.saveUser(platform, bigGoogleUserId, name, profileImage, googleAccessToken, email);

        // JWT 생성
        String jwtToken = generateTokenForUser(bigGoogleUserId);

        return new GoogleAuthResponseDto(
                "구글 토큰 처리 완료",
                jwtToken,
                name,
                profileImage,
                googleAccessToken,
                platform,
                email
        );
    }

    public KakaoAuthResponseDto authenticateKakaoUser(String authorizationCode , String clientRedirectUri) {
        // redirect_uri 검증
        if (!isValidRedirectUri(clientRedirectUri)) {
            throw new IllegalArgumentException("Invalid redirect URI");
        }

        String newClientRedirectUri = redirectUriOrigin;
        if (clientRedirectUri.equals(apiServerOrigin)){
            newClientRedirectUri = redirectUriOrigin;
        }



        if (clientRedirectUri.equals(apiServerOrigin+"/result?redirected=true")) {
            newClientRedirectUri =  redirectUriOrigin+"/result?redirected=true";
        }

        // 액세스 토큰 요청
        String tokenUrl = KAKAO_TOKEN_URL +
                "?grant_type=authorization_code" +
                "&client_id=" + kakaoJsKey +
                "&redirect_uri=" + newClientRedirectUri +
                "&code=" + authorizationCode;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, Map.class);

        String kakaoAccessToken = (String) response.getBody().get("access_token");

        // 사용자 정보 요청
        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.set("Authorization", "Bearer " + kakaoAccessToken);
        HttpEntity<String> userInfoEntity = new HttpEntity<>(userInfoHeaders);
        ResponseEntity<Map> userInfoResponse = restTemplate.exchange(KAKAO_USER_INFO_URL, HttpMethod.GET, userInfoEntity, Map.class);

        // 사용자 정보 파싱
        Map<String, Object> userInfo = userInfoResponse.getBody();
        Long kakaoUserId = (Long) userInfo.get("id");
        Map<String, Object> properties = (Map<String, Object>) userInfo.get("properties");
        String nickname = (String) properties.get("nickname");
        String profileImage = (String) properties.get("profile_image");
        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
        String email = (String) kakaoAccount.get("email");
        String platform = "kakao";
        BigInteger bigkakaoUserId = BigInteger.valueOf(kakaoUserId);

        // 사용자 정보 저장
        userService.saveUser(platform, bigkakaoUserId, nickname, profileImage, kakaoAccessToken, email);

        // JWT 생성
        String jwtToken = generateTokenForUser(bigkakaoUserId);

        User user = userService.findBySocialId(bigkakaoUserId);
        if (user != null) {
            nickname = user.getNickname();
            profileImage = user.getUserImage();
        }

        return new KakaoAuthResponseDto(
                "카카오 토큰 처리 완료",
                jwtToken,
                nickname,
                profileImage,
                kakaoAccessToken,
                platform,
                user.getCreatedAt(),
                email
        );
    }

    private String generateTokenForUser(BigInteger userId) {
        return generateToken(userId);
    }

    public ResponseEntity<?> performKakaoUnlink(String token) {
        // 쿠키에서 JWT 추출
        if (token == null || !jwtUtils.validateJwtToken(token)) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid or expired token."));
        }

        // JWT에서 socialId 추출
        String socialIdString = jwtUtils.getUsernameFromJwtToken(token);
        BigInteger socialId = new BigInteger(socialIdString);

        // 데이터베이스에서 사용자 정보 조회
        User user = userService.findBySocialId(socialId);
        if (user == null || user.getAccessToken() == null) {
            return ResponseEntity.badRequest().body("User not found or access token is missing.");
        }

        // 카카오 로그아웃 API 호출
        String kakaoAccessToken = user.getAccessToken();
        String kakaoLogoutUrl = "https://kapi.kakao.com/v1/user/unlink";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + kakaoAccessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.postForEntity(kakaoLogoutUrl, entity, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.ok()
                    .body("Kakao unlink successful. Response: " + response.getBody());
        } else {
            return ResponseEntity.status(response.getStatusCode())
                    .body("Failed to unlink from Kakao. Response: " + response.getBody());
        }
    }

    public ResponseEntity<?> deleteUser(String jwtToken) {
        BigInteger socialId = extractSocialIdFromToken(jwtToken);
        User user = userService.findBySocialId(socialId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid user."));
        }

        String platform = user.getSocialPlatform();

        // 플랫폼별 로그아웃 처리
        ResponseEntity<?> logoutResponse;
        if ("kakao".equals(platform)) {
            logoutResponse = performKakaoUnlink(jwtToken);
        } else if ("google".equals(platform)) {
            logoutResponse = performGoogleLogout(jwtToken);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Unsupported platform."));
        }

        if (!logoutResponse.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Logout failed."));
        }
        // Soft Delete
        boolean response = userService.deleteUserBySocialId(socialId);

        if (response) {// 토큰 해제
            ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", "")
                    .httpOnly(false)// 테스트용 - false
                    .secure(true) // 필요에 따라 설정
                    .path("/")
                    .maxAge(0)
                    .sameSite("Strict")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(Map.of("success", true, "message", "Google logout and user deletion successful."));
        } else {
            // 실패 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Failed to delete user."));
        }
    }

    private BigInteger extractSocialIdFromToken(String jwtToken) {
        return new BigInteger(jwtUtils.getUsernameFromJwtToken(jwtToken));
    }

    public boolean isValidRedirectUri(String clientRedirectUri) {
        List<String> trimmedUris = allowedRedirectUris.stream()
                .map(String::trim)
                .collect(Collectors.toList());

        return trimmedUris.contains(clientRedirectUri.trim());
    }
}
