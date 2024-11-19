package com.spring.be.user.controller;

import com.spring.be.entity.User;
import com.spring.be.jwt.config.JwtUtils;
import com.spring.be.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigInteger;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    private final JwtUtils jwtUtils;

    @Autowired
    public UserController(JwtUtils jwtUtils, UserService userService) {
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    @Autowired
    private UserService userService; // 사용자 서비스 인터페이스 주입

    @PostMapping("/update-profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader("Cookie") String cookieHeader,
            @RequestParam("nickname") String nickname,
            @RequestParam(value = "profileImage", required = false) String profileImage) {
        try {
            // 사용자 정보 업데이트 로직 (현재 사용자의 ID를 가져오는 방법에 따라 변경 필요)

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

            user.setNickname(nickname);

            // 프로필 이미지 처리 (파일 저장 또는 URL 저장)
            System.out.println("check"+profileImage);
            if (profileImage != null && !profileImage.isEmpty()) {
                user.setUserImage(profileImage);
            }

            userService.saveUserProfile(user, nickname, profileImage); // 사용자 정보 DB에 저장

            return ResponseEntity.ok(Map.of(
                    "updatedNickname", user.getNickname(),
                    "updatedProfileImage", user.getUserImage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("프로필 업데이트 중 오류가 발생했습니다.");
        }
    }

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
