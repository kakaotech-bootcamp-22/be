package com.spring.be.reviewcheck.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewQueuePayloadDto {
    private String requestId;
    private String blogUrl;
}
