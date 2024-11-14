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

    public User saveUser(String platform, Long kakaoUserId, String nickname, String profileImage) {
        User user = new User(platform, kakaoUserId, nickname, profileImage);
        return userRepository.save(user);
    }
}
