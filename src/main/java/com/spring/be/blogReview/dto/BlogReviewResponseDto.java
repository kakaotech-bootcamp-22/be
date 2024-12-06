package com.spring.be.blogReview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogReviewResponseDto {
    private RatingStatsDto ratingStats;
    private Page<ReviewDto> reviews;
    private int totalReviews;
    private int totalPages;
}
