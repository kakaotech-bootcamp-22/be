package com.spring.be.jwt.service;

import com.spring.be.entity.User;
import com.spring.be.jwt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    // socialId로 사용자 존재 여부 확인 후, 없으면 새로 추가, 있으면 업데이트
    public User saveUser(String platform, Long socialId, String nickname, String profileImage, String kakaoAccessToken) {
        // 기존 사용자 찾기
        User existingUser = userRepository.findBySocialId(socialId);

        if (existingUser != null) {
            // 기존 사용자 정보 업데이트
            existingUser.setNickname(nickname);
            existingUser.setUserImage(profileImage);
            existingUser.setKakaoAccessToken(kakaoAccessToken);
            return userRepository.save(existingUser); // 기존 사용자 업데이트
        } else {
            // 새로운 사용자 저장
            User newUser = new User(platform, socialId, nickname, profileImage, kakaoAccessToken);
            return userRepository.save(newUser); // 새로운 사용자 저장
        }
    }

    public User findBySocialId(Long socialId) {
        return userRepository.findBySocialId(socialId);
    }


}
