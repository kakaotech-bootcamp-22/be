package com.spring.be.jwt.controller;

import com.spring.be.jwt.config.JwtUtils;
import com.spring.be.jwt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AuthController {

    private final JwtUtils jwtUtils;
    private final UserService userService;


    @Autowired
    public AuthController(JwtUtils jwtUtils, UserService userService) {
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    @PostMapping("/auth/token")
    public String generateToken(@RequestBody Map<String, Object> userInfo) {
        Long kakaoUserId = (Long) userInfo.get("kakaoUserId");

        String userId = String.valueOf(kakaoUserId);

        String jwtToken = jwtUtils.generateJwtToken(userId);

        return jwtToken;
    }

    // 로그인 상태 확인을 위한 API
    @GetMapping("/auth/status")
    public ResponseEntity<?> checkLoginStatus(@RequestHeader("Cookie") String cookieHeader) {
        // 쿠키에서 JWT 추출
        String token = extractTokenFromCookie(cookieHeader, "jwtToken");

        if (token == null || !jwtUtils.validateJwtToken(token)) {
            return ResponseEntity.ok().body(Map.of("isLoggedIn", false, "message", "Invalid or expired token."));
        }

        return ResponseEntity.ok().body(Map.of("isLoggedIn", true, "message", "User is logged in."));
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

}
