package com.spring.be.jwt.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutResponseDto {
    private boolean success;
    private String message;

    public LogoutResponseDto(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
