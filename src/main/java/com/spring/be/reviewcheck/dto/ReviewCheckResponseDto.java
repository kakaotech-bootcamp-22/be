package com.spring.be.reviewcheck.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReviewCheckResponseDto {
    private String requestId;
    private String blogUrl;
    private String summaryTitle;
    private String summaryText;
    private Integer score;
    private String evidence;
}
