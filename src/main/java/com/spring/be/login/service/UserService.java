package com.spring.be.login.service;

import com.spring.be.jwt.config.JwtUtils;
import com.spring.be.login.entity.User;
import com.spring.be.login.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtils jwtUtils;

    // 회원가입 기능
    public void register(String username, String password) {
        // 이미 존재하는 사용자 이름인지 확인
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 존재하는 사용자 이름입니다.");
        }

        // 비밀번호 암호화 구현해야함
//        String encodedPassword = passwordEncoder.encode(password);

        // 새로운 사용자 객체 생성
        User user = User.builder()
                .username(username)
                .password(password)
                .build();

        // 사용자 저장
        userRepository.save(user);
    }

    // 로그인 기능
    public String login(String username, String password) {
        // 사용자 이름으로 사용자 검색
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }
        User user = userOptional.get();
        // 비밀번호 확인
        if (!password.equals(user.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        // JWT 토큰 생성 및 반환
        return jwtUtils.generateJwtToken(username);
    }
}
