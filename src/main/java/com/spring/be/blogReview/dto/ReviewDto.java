package com.spring.be.blogReview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private Long blogReviewId;
    private int rating;
    private LocalDateTime date;
    private String content;
    private String author;
    private String profileImage;
    private int likes;
}