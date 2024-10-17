package com.spring.be.login.controller;

import com.spring.be.jwt.config.JwtUtils;
import com.spring.be.login.dto.LoginRequestDto;
import com.spring.be.login.dto.SignupRequestDto;
import com.spring.be.login.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserService userService;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequestDto LoginRequestDto) {
        String login = userService.login(LoginRequestDto.getUsername(), LoginRequestDto.getPassword());
        return ResponseEntity.ok(login);
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequestDto signUpRequest) {

        userService.register(signUpRequest.getUsername(), signUpRequest.getPassword());

        return ResponseEntity.ok("User registered successfully!");
    }

}
