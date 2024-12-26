package com.spring.be.jwt.controller;

import com.spring.be.entity.User;
import com.spring.be.jwt.dto.KakaoAuthResponseDto;
import com.spring.be.jwt.service.AuthService;
import com.spring.be.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth/kakao")

public class KakaoAuthController {

    private final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    private final UserService userService;
    private final AuthService authService;

    @Value("${REACT_APP_KAKAO_JS_KEY:default-value}")
    private String kakaoJsKey;

    @Autowired
    public KakaoAuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/token")
    public ResponseEntity<KakaoAuthResponseDto> handleKakaoToken(@RequestBody Map<String, String> request) {
        String authorizationCode = request.get("code");
        String clientRedirectUri = request.get("redirectUri");
        if (authorizationCode == null || authorizationCode.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        // AuthService에 Kakao 인증 처리를 위임
        KakaoAuthResponseDto responseDto = authService.authenticateKakaoUser(authorizationCode, clientRedirectUri);

        if (responseDto == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        // JWT를 쿠키에 설정
        ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", responseDto.getJwtToken())
                .httpOnly(false)// 테스트용 - false
                .secure(false)
                .path("/")
                .maxAge(60 * 60 * 24)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(responseDto);
    }
}