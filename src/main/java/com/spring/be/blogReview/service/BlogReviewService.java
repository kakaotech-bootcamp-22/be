package com.spring.be.blogReview.service;

import com.spring.be.blogReview.dto.BlogReviewResponseDto;
import com.spring.be.blogReview.dto.RatingStatsDto;
import com.spring.be.blogReview.dto.ReviewDto;
import com.spring.be.blogReview.dto.ReviewRequest;
import com.spring.be.blogReview.repository.BlogRepository;
import com.spring.be.blogReview.repository.BlogReviewRepository;
import com.spring.be.entity.Blog;
import com.spring.be.entity.BlogReview;
import com.spring.be.entity.User;
import com.spring.be.jwt.config.JwtUtils;
import com.spring.be.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
public class BlogReviewService {

    @Autowired
    private BlogReviewRepository blogReviewRepository;
    @Autowired
    private BlogRepository blogRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtils jwtUtils;

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

    //totalReviews 가져오기
    public Integer getBlogReviewCount(Long blogId) {
        return blogRepository.findReviewCountByBlogId(blogId);
    }

    // BlogReviewResponseDto 생성
    public BlogReviewResponseDto getBlogReviewResponse(Long blogId) {
        RatingStatsDto stats = getRatingStats(blogId);
        int size = 5;
        Page<ReviewDto> reviews = getReviewsByLikesCnt(blogId, 0, size);
        int totalReviews = getBlogReviewCount(blogId);
        int totalPages = (int) Math.ceil((double) totalReviews / size);

        return new BlogReviewResponseDto(stats, reviews, totalReviews,totalPages);
    }

    // 리뷰 등록 로직
    public Long saveReview(ReviewRequest request, BigInteger socialId) {
        Blog blog = blogRepository.findById(request.getBlogId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid blog ID"));

        User user = userRepository.findBySocialId(socialId);

        BlogReview review = BlogReview.builder()
                .blog(blog)
                .user(user)
                .rating(request.getRating())
                .content(request.getContent())
                .likesCnt(0)
                .build();

        blogReviewRepository.save(review);

        blog.setReviewCount(blog.getReviewCount() + 1);
        blogRepository.save(blog);

        return review.getBlogReviewId();
    }

    public void incrementLikes(Long reviewId) {
        BlogReview review = blogReviewRepository.findByBlogReviewId(reviewId);
        review.setLikesCnt(review.getLikesCnt() + 1);
        blogReviewRepository.save(review);
    }

    public Page<ReviewDto> getReviewsByLikesCnt(Long blogId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return blogReviewRepository.findByLikesCntDesc(blogId, pageable);
    }

    public Page<ReviewDto> getReviewsByCreatedAt(Long blogId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return blogReviewRepository.findByCreatedAtDesc(blogId, pageable);
    }

    public Page<ReviewDto> getReviewsByRatingDesc(Long blogId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return blogReviewRepository.findByRatingDesc(blogId, pageable);
    }

    public Page<ReviewDto> getReviewsByRatingAsc(Long blogId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return blogReviewRepository.findByRatingAsc(blogId, pageable);
    }
}
