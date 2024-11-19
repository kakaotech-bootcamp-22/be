package com.spring.be.user.service;

import com.spring.be.entity.User;
import com.spring.be.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    // socialId로 사용자 존재 여부 확인 후, 없으면 새로 추가, 있으면 업데이트
    public User saveUser(String platform, BigInteger socialId, String nickname, String profileImage, String AccessToken) {
        // 기존 사용자 찾기
        User existingUser = userRepository.findBySocialId(socialId);

        if (existingUser != null) {
            // 기존 사용자 정보 업데이트
//            existingUser.setNickname(nickname);
//            existingUser.setUserImage(profileImage);
            existingUser.setAccessToken(AccessToken);
            return userRepository.save(existingUser);
        } else {
            // 새로운 사용자 저장
            User newUser = new User(platform, socialId, nickname, profileImage, AccessToken);
            return userRepository.save(newUser); // 새로운 사용자 저장
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





}
