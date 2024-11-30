package com.spring.be.blogReview.service;

import com.spring.be.blogReview.dto.BlogReviewResponseDto;
import com.spring.be.blogReview.dto.RatingStatsDto;
import com.spring.be.blogReview.dto.ReviewDto;
import com.spring.be.blogReview.repository.BlogReviewRepository;
import com.spring.be.entity.BlogReview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlogReviewService {

    @Autowired
    private BlogReviewRepository blogReviewRepository;

    //ratingStats 계산
    public RatingStatsDto getRatingStats(Long blogId) {

        List<BlogReview> reviews = blogReviewRepository.findByBlogBlogId(blogId);

        RatingStatsDto stats = RatingStatsDto.builder()
                .one(0)
                .two(0)
                .three(0)
                .four(0)
                .five(0)
                .build();

        for (BlogReview review : reviews) {
            switch (review.getRating()) {
                case 1 -> stats.setOne(stats.getOne() + 1);
                case 2 -> stats.setTwo(stats.getTwo() + 1);
                case 3 -> stats.setThree(stats.getThree() + 1);
                case 4 -> stats.setFour(stats.getFour() + 1);
                case 5 -> stats.setFive(stats.getFive() + 1);
            }
        }

        return stats;
    }

    //reviews 가져오기
    public List<ReviewDto> getReviews(Long blogId) {
        return blogReviewRepository.findReviewsByBlogId(blogId);
    }

    //totalReviews 가져오기
    public long getBlogReviewCount(Long blogId) {
        return blogReviewRepository.countByBlogBlogId(blogId);
    }

    // BlogReviewResponseDto 생성
    public BlogReviewResponseDto getBlogReviewResponse(Long blogId) {
        RatingStatsDto stats = getRatingStats(blogId);
        List<ReviewDto> reviews = getReviews(blogId);
        long totalReviews = getBlogReviewCount(blogId);

        return new BlogReviewResponseDto(stats, reviews, totalReviews);
    }
}
