package com.spring.be.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileResponseDto {
    private  boolean isLoggedIn;
    private String updatedNickname;
    private String updatedProfileImage;
    private String message;
}
