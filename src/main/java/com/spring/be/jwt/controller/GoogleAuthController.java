package com.spring.be.jwt.controller;

import com.spring.be.jwt.dto.GoogleAuthResponseDto;
import com.spring.be.jwt.service.AuthService;
import com.spring.be.user.service.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/auth/google")
public class GoogleAuthController {

    private final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final UserService userService;
    private final AuthService authService;

    @Value("${REACT_APP_GOOGLE_CLIENT_ID:default-value}")
    private String googleClientId;

    @Autowired
    public GoogleAuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/token")
    public ResponseEntity<GoogleAuthResponseDto> handleGoogleToken(@RequestBody Map<String, String> request) {
        String googleAccessToken = request.get("access_token");
        if (googleAccessToken == null || googleAccessToken.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        // AuthService에 Google 로그인 처리를 위임
        GoogleAuthResponseDto responseDto = authService.handleGoogleAuthentication(googleAccessToken);

        if (responseDto == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        // JWT 쿠키 설정
        ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", responseDto.getJwtToken())
                .httpOnly(true)
                .secure(false) // 필요에 따라 설정
                .path("/")
                .maxAge(60 * 60 * 24)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", jwtCookie.toString())
                .body(responseDto);
    }
}