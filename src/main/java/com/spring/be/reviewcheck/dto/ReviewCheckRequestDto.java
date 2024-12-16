package com.spring.be.reviewcheck.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReviewCheckRequestDto {
    @NotBlank
    private String blogUrl;
}
