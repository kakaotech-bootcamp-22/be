package com.spring.be.jwt.service;

import com.spring.be.jwt.config.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final JwtUtils jwtUtils;

    @Autowired
    public AuthService(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    public String generateToken(String userId) {
        return jwtUtils.generateJwtToken(userId);
    }

}
