package com.spring.be.blogReview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogReviewResponseDto {
    private RatingStatsDto ratingStats;
    private List<ReviewDto> reviews;
    private long totalReviews;
}