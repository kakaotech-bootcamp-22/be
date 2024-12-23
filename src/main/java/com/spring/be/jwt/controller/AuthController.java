package com.spring.be.jwt.controller;

import com.spring.be.jwt.config.JwtUtils;
import com.spring.be.jwt.dto.CommonResponseDto;
import com.spring.be.jwt.dto.LogoutResponseDto;
import com.spring.be.jwt.dto.UserLoginStatusDto;
import com.spring.be.jwt.service.AuthService;
import com.spring.be.user.dto.UserResponseDto;
import com.spring.be.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<UserLoginStatusDto> checkLoginStatus(@CookieValue("jwtToken") String jwtToken) {
        UserLoginStatusDto response = authService.checkLoginStatus(jwtToken);

        if (!response.isLoggedIn()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout/kakao")
    public ResponseEntity<CommonResponseDto> kakaoLogout(@CookieValue("jwtToken") String jwtToken) {
        CommonResponseDto response = authService.performKakaoLogout(jwtToken);

        if(!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

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
                .body(response);
    }

    @PostMapping("/logout/google")
    public ResponseEntity<CommonResponseDto> googleLogout(@CookieValue("jwtToken") String jwtToken) {
        CommonResponseDto response = authService.performGoogleLogout(jwtToken);

        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", "")
                .httpOnly(false)// 테스트용 - false
                .secure(true) // 필요에 따라 설정
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(response);
    }

    @GetMapping("/delete")
    public ResponseEntity<CommonResponseDto> deleteUser(@CookieValue("jwtToken") String jwtToken) {
        CommonResponseDto response = authService.deleteUser(jwtToken);

        if(!response.isSuccess()) {
            if (response.getMessage().equals("Invalid user.")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            } else if (response.getMessage().equals("Unsupported platform.")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            } else if (response.getMessage().equals("Logout failed.") || response.getMessage().equals("Failed to delete user.")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }

        ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", "")
                .httpOnly(false)// 테스트용 - false
                .secure(true) // 필요에 따라 설정
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(response);
    }
}
