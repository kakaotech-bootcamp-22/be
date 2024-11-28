package com.spring.be.jwt.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleAuthResponseDto {
    private String message;
    private String jwtToken;
    private String nickname;
    private String profileImage;
    private String googleAccessToken;
    private String platform;
    private String email;

    public GoogleAuthResponseDto(String message, String jwtToken, String nickname, String profileImage, String googleAccessToken, String platform, String email) {
        this.message = message;
        this.jwtToken = jwtToken;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.googleAccessToken = googleAccessToken;
        this.platform = platform;
        this.email = email;
    }
}
