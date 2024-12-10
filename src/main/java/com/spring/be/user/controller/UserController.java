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
            @CookieValue("jwtToken") String jwtToken,
            @RequestParam("nickname") String nickname,
            @RequestParam(value = "profileImage", required = false) String profileImage) {

        BigInteger socialId = extractSocialIdFromCookie(jwtToken);
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
    }

    // 유저 활동 데이터 조회
    @GetMapping("/activity-counts")
    public ResponseEntity<?> getUserActivityCounts(@CookieValue("jwtToken") String jwtToken) {
        BigInteger socialId = extractSocialIdFromCookie(jwtToken);
        Long userId = userRepository.findUserIdBySocialId(socialId);
        UserActivityCountsDto activityCounts = userService.getUserActivityCounts(userId);
        return ResponseEntity.ok(activityCounts);
    }

    private BigInteger extractTokenFromCookie(String jwtToken) {
        if (jwtToken == null || jwtToken.isEmpty()) {
            return null;
        }
        return new BigInteger(jwtUtils.getUsernameFromJwtToken(jwtToken)); // socialId 반환
    }

    private BigInteger extractSocialIdFromCookie(String jwtToken) {
        if (jwtToken == null || !jwtUtils.validateJwtToken(jwtToken)) {
            throw new IllegalArgumentException("유효하지 않은 JWT 토큰입니다.");
        }
        return new BigInteger(jwtUtils.getUsernameFromJwtToken(jwtToken)); // socialId만 반환
    }
}
