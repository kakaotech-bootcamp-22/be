package com.spring.be.jwt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth/kakao")
public class KakaoAuthController {

    @PostMapping("/token")
    public ResponseEntity<String> handleKakaoToken(@RequestBody Map<String, String> request) {
        String kakaoAccessToken = request.get("token");

        return ResponseEntity.ok("카카오 토큰 처리 완료");
    }
}
