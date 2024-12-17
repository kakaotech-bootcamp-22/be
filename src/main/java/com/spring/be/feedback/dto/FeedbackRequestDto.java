package com.spring.be.feedback.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FeedbackRequestDto {
    private Long resultId;
    private Boolean type;
    private String reason;
}
