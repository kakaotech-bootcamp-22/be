package com.spring.be.user.service;

import com.spring.be.blogReview.service.BlogReviewService;
import com.spring.be.entity.User;
import com.spring.be.user.dto.UserActivityCountsDto;
import com.spring.be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BlogReviewService blogReviewService;

    // socialId로 사용자 존재 여부 확인 후, 없으면 새로 추가, 있으면 업데이트
    public User saveUser(String platform, BigInteger socialId, String nickname, String profileImage, String AccessToken, String email) {
        // 기존 사용자 찾기
        User existingUser = userRepository.findBySocialId(socialId);
        if (existingUser != null) {
            // 기존 사용자 정보 업데이트
            existingUser.setAccessToken(AccessToken);
            existingUser.setIsDeleted(false);
            existingUser.setDeletedAt(null);
            return userRepository.save(existingUser);
        }
        try {
            User newUser = new User(platform, socialId, nickname, profileImage, AccessToken, email);
            return userRepository.save(newUser);
        } catch (DataIntegrityViolationException e) {
            // 중복 발생 시 기존 사용자 반환
            return userRepository.findBySocialId(socialId);
        }
    }

    public User findBySocialId(BigInteger socialId) {
        return userRepository.findBySocialId(socialId);
    }

    public User saveUserProfile(User user, String newNickname, String newProfileImage) {
        if (newNickname != null && !newNickname.isEmpty()) {
            user.setNickname(newNickname);
        }
        if (newProfileImage != null && !newProfileImage.isEmpty()) {
            user.setUserImage(newProfileImage);
        }
        return userRepository.save(user);
    }

    public UserActivityCountsDto getUserActivityCounts(Long userId) {
        int reviewCount = blogReviewService.getReviewCountByUserId(userId);
        int likeCount = blogReviewService.getTotalLikesReceived(userId);

        return new UserActivityCountsDto(reviewCount, likeCount);
    }

    public boolean deleteUserBySocialId(BigInteger socialId) {
        User user = userRepository.findBySocialId(socialId);

        if (user == null) {
            return false;
        }

        user.setIsDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
        return true;
    }
}
