package com.spring.be.jwt.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spring.be.util.CustomLocalDateTimeSerializer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserLoginStatusDto {
    private boolean isLoggedIn;
    private String message;
    private String nickname;
    private String userImage;
    private String platform;
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class) // Custom serializer 적용
    private LocalDateTime createdAt;
    private String email;

    public UserLoginStatusDto(boolean isLoggedIn, String message, String nickname, String userImage, String platform, LocalDateTime createdAt, String email) {
        this.isLoggedIn = isLoggedIn;
        this.message = message;
        this.nickname = nickname;
        this.userImage = userImage;
        this.platform = platform;
        this.createdAt = createdAt;
        this.email = email;
    }

    @Override
    public String toString() {
        return "UserLoginStatusDto{" +
                "isLoggedIn=" + isLoggedIn +
                ", message='" + message + '\'' +
                ", nickname='" + nickname + '\'' +
                ", userImage='" + userImage + '\'' +
                ", platform='" + platform + '\'' +
                ", createdAt=" + createdAt + '\'' +
                ", email='" + email+
                '}';
    }
}


