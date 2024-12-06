package com.spring.be.user.controller;

import com.spring.be.entity.User;
import com.spring.be.jwt.config.JwtUtils;
import com.spring.be.user.dto.UserActivityCountsDto;
import com.spring.be.user.repository.UserRepository;
import com.spring.be.user.service.S3Service;
import com.spring.be.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigInteger;
import java.util.Map;

@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class UserController {

    private final JwtUtils jwtUtils;
    private final S3Service s3Service;
    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping("/update-profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader("Cookie") String cookieHeader,
            @RequestParam("nickname") String nickname,
            @RequestParam(value = "profileImage", required = false) String profileImage) {
        try {
            BigInteger socialId = extractSocialIdFromCookie(cookieHeader);
            User user = userService.findBySocialId(socialId); // socialId로 사용자 찾기
            if (user == null) {
                return ResponseEntity.ok().body(Map.of("isLoggedIn", false, "message", "User not found."));
            }

            user.setNickname(nickname);
            // 프로필 이미지 처리 (파일 저장 또는 URL 저장)

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

    // 유저 활동 데이터 조회
    @GetMapping("/activity-counts")
    public ResponseEntity<?> getUserActivityCounts(@RequestHeader("Cookie") String cookieHeader) {
        try {
            BigInteger socialId = extractSocialIdFromCookie(cookieHeader);
            Long userId = userRepository.findUserIdBySocialId(socialId);
            UserActivityCountsDto activityCounts = userService.getUserActivityCounts(userId);
            return ResponseEntity.ok(activityCounts);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "잘못된 요청: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // 디버깅용 로그
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "서버 내부 오류가 발생했습니다."));
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

    private BigInteger extractSocialIdFromCookie(String cookieHeader) {
        String token = extractTokenFromCookie(cookieHeader, "jwtToken");

        if (token == null || !jwtUtils.validateJwtToken(token)) {
            throw new IllegalArgumentException("유효하지 않은 JWT 토큰입니다.");
        }

        return new BigInteger(jwtUtils.getUsernameFromJwtToken(token)); // socialId만 반환
    }
}
