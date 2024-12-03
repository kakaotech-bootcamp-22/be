package com.spring.be.jwt.controller;

import com.spring.be.jwt.config.JwtUtils;
import com.spring.be.jwt.service.AuthService;
import com.spring.be.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.spring.be.util.CookieUtils;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    @Autowired
    public AuthController(AuthService authService, JwtUtils jwtUtils, UserService userService) {
        this.authService = authService;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    @PostMapping("/token")
    public String generateToken(@RequestBody Map<String, Object> userInfo) {
        Long kakaoUserId = (Long) userInfo.get("kakaoUserId");
        return authService.generateToken(kakaoUserId);
    }

    @GetMapping("/status")
    public ResponseEntity<?> checkLoginStatus(@RequestHeader("Cookie") String cookieHeader) {
        String token = CookieUtils.extractTokenFromCookie(cookieHeader, "jwtToken");
        return authService.checkLoginStatus(token);
    }

    @PostMapping("/logout/kakao")
    public ResponseEntity<?> kakaoLogout(@RequestHeader("Cookie") String cookieHeader) {
        String token = CookieUtils.extractTokenFromCookie(cookieHeader, "jwtToken");
        return authService.performKakaoLogout(token);
    }

    @PostMapping("/logout/google")
    public ResponseEntity<?> googleLogout(@RequestHeader("Cookie") String cookieHeader) {
        String token = CookieUtils.extractTokenFromCookie(cookieHeader, "jwtToken");
        return authService.performGoogleLogout(token);
    }
}