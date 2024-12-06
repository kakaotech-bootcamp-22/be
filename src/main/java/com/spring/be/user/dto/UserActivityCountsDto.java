package com.spring.be.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class UserActivityCountsDto {
    private final int reviewCount;
    private final int likeCount;
}

