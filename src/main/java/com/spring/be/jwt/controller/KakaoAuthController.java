package com.spring.be.jwt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth/kakao")

public class KakaoAuthController {
    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/token")
    public ResponseEntity<String> handleKakaoToken(@RequestBody Map<String, String> request) {
        String kakaoAccessToken = request.get("token");
        System.out.println("Received Kakao access token: " + kakaoAccessToken); // 로그 추가
        return ResponseEntity.ok("카카오 토큰 처리 완료");
    }
}
