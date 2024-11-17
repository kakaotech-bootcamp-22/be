package com.spring.be.jwt.controller;

import com.spring.be.jwt.service.AuthService;
import com.spring.be.jwt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.util.Map;

@RestController
@RequestMapping("/auth/google")
public class GoogleAuthController {

    private final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final UserService userService;
    private final AuthService authService;

    @Autowired
    public GoogleAuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/token")
    public ResponseEntity<String> handleGoogleToken(@RequestBody Map<String, String> request) {
        String googleAccessToken = request.get("access_token");
        if (googleAccessToken == null || googleAccessToken.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid access token");
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + googleAccessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
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

            // 사용자 정보 출력
            System.out.println("Google User ID: " + googleUserId);
            System.out.println("Name: " + name);
            System.out.println("Email: " + email);
            System.out.println("Profile Image URL: " + profileImage);

            // 소셜로그인 플랫폼, 소셜 계정 아이디, 닉네임, 프로필 이미지 DB에 저장
            userService.saveUser(platform, bigGoogleUserId, name, profileImage, googleAccessToken);

            // JWT 생성
            String userId = String.valueOf(googleUserId);
            String jwtToken = authService.generateToken(userId);

            // JWT를 쿠키에 설정

            ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", jwtToken)
                    .httpOnly(true) // JavaScript 접근 불가
                    .secure(false) // HTTPS에서만 전송 (필요에 따라 설정)
                    .path("/") // 모든 경로에서 유효
                    .maxAge(60 * 60 * 24) // 24시간 유효
                    .sameSite("Strict") // CSRF 방지 설정
                    .build();

            return ResponseEntity.ok()
                    .header("Set-Cookie", jwtCookie.toString())
                    .body("{\"message\": \"구글 토큰 처리 완료\", \"jwtToken\": \"" + jwtToken + "\", \"nickname\": \"" + name + "\", \"profileImage\": \"" + profileImage + "\",  \"googleAccessToken\": \"" + googleAccessToken + "\" , \"platform\":  \"" + platform + "\" }");

        } catch (Exception e) {
            // 예외 처리
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving user information");
        }

    }
}