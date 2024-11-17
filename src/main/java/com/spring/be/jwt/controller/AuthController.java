package com.spring.be.jwt.controller;

import com.spring.be.entity.User;
import com.spring.be.jwt.config.JwtUtils;
import com.spring.be.jwt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @Autowired
    public AuthController(JwtUtils jwtUtils, UserService userService) {
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    @PostMapping("/token")
    public String generateToken(@RequestBody Map<String, Object> userInfo) {
        Long kakaoUserId = (Long) userInfo.get("kakaoUserId");

        String userId = String.valueOf(kakaoUserId);

        String jwtToken = jwtUtils.generateJwtToken(userId);

        return jwtToken;
    }

    // 로그인 상태 확인을 위한 API
    @GetMapping("/status")
    public ResponseEntity<?> checkLoginStatus(@RequestHeader("Cookie") String cookieHeader) {
        // 쿠키에서 JWT 추출
        String token = extractTokenFromCookie(cookieHeader, "jwtToken");

        if (token == null || !jwtUtils.validateJwtToken(token)) {
            return ResponseEntity.ok().body(Map.of("isLoggedIn", false, "message", "Invalid or expired token."));
        }

        // JWT에서 socialId 추출
        String socialIdString = jwtUtils.getUsernameFromJwtToken(token);  // 여기서는 userId 대신 socialId를 사용
        BigInteger socialId = new BigInteger(socialIdString);

        // 사용자 정보(DB에서 가져오기)
        User user = userService.findBySocialId(socialId); // socialId로 사용자 찾기
        if (user == null) {
            return ResponseEntity.ok().body(Map.of("isLoggedIn", false, "message", "User not found."));
        }

        return ResponseEntity.ok().body(Map.of(
                "isLoggedIn", true,
                "message", "User is logged in.",
                "nickname", user.getNickname(),  // 닉네임 반환
                "userImage", user.getUserImage(), // 사용자 이미지 반환
                "platform", user.getSocialPlatform() // 플랫폼 반환
        ));
    }

    // 쿠키 헤더에서 JWT 추출
    private String extractTokenFromCookie(String cookieHeader, String cookieName) {
        if (cookieHeader == null || cookieHeader.isEmpty()) {
            return null;
        }

        // "jwtToken=eyJhb...; Path=/" 형식의 문자열에서 토큰 값을 추출
        String[] cookies = cookieHeader.split("; ");
        for (String cookie : cookies) {
            if (cookie.startsWith(cookieName + "=")) {
                return cookie.substring((cookieName + "=").length());
            }
        }
        return null;
    }

    @PostMapping("/logout/kakao")
    public ResponseEntity<?> kakaoLogout(@RequestHeader("Cookie") String cookieHeader) {
        // 쿠키에서 JWT 추출
        String token = extractTokenFromCookie(cookieHeader, "jwtToken");
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

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(kakaoLogoutUrl, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                // 로그아웃 성공시 사용자 세션 및 쿠키 해제
                ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", "")
                        .httpOnly(true)
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
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Kakao logout failed: " + e.getMessage());
        }
    }

    @PostMapping("/logout/google")
    public ResponseEntity<?> googleLogout(@RequestHeader("Cookie") String cookieHeader) {
        // 쿠키에서 JWT 추출
        String token = extractTokenFromCookie(cookieHeader, "jwtToken");
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
                    .httpOnly(true)
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
}