package com.spring.be.feedback.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FeedbackResponseDto {
    private Long feedbackId;
    private Long resultId;
    private Boolean type;
    private String reason;
}
