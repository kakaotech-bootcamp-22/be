package com.spring.be.jwt.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spring.be.util.CustomLocalDateTimeSerializer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class KakaoAuthResponseDto {
    private String message;
    private String jwtToken;
    private String nickname;
    private String profileImage;
    private String kakaoAccessToken;
    private String platform;
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class) // Custom serializer 적용
    private LocalDateTime createdAt;
    private String email;

    public KakaoAuthResponseDto(String message, String jwtToken, String nickname, String profileImage, String kakaoAccessToken, String platform, LocalDateTime createdAt, String email) {
        this.message = message;
        this.jwtToken = jwtToken;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.kakaoAccessToken = kakaoAccessToken;
        this.platform = platform;
        this.createdAt = createdAt;
        this.email = email;
    }
}
