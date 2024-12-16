package com.spring.be.reviewcheck.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCheckResponseDto {
    private String requestId;
    private String blogUrl;
    private String summaryTitle;
    private String summaryText;
    private Integer score;
    private String evidence;
}
