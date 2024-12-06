package com.spring.be.blogReview.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequest {
    private Integer rating; // 별점 (1~5)
    private String content; // 리뷰 내용
    private Long blogId;    // 블로그 ID
}
