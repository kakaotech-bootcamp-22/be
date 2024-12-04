package com.spring.be.blogReview.service;

import com.spring.be.blogReview.dto.BlogReviewResponseDto;
import com.spring.be.blogReview.dto.RatingStatsDto;
import com.spring.be.blogReview.dto.ReviewDto;
import com.spring.be.blogReview.dto.ReviewRequest;
import com.spring.be.blogReview.repository.BlogRepository;
import com.spring.be.blogReview.repository.BlogReviewRepository;
import com.spring.be.blogReview.repository.ReviewLikeRepository;
import com.spring.be.entity.Blog;
import com.spring.be.entity.BlogReview;
import com.spring.be.entity.ReviewLike;
import com.spring.be.entity.User;
import com.spring.be.jwt.config.JwtUtils;
import com.spring.be.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
public class BlogReviewService {

    @Autowired
    private BlogReviewRepository blogReviewRepository;
    @Autowired
    private BlogRepository blogRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReviewLikeRepository reviewLikeRepository;
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
    public BlogReviewResponseDto getBlogReviewResponse(Long blogId, BigInteger socialId) {
        RatingStatsDto stats = getRatingStats(blogId);
        int size = 5;
        Page<ReviewDto> reviews = getReviews(blogId, socialId, 0, size, "likes");
        int totalReviews = getBlogReviewCount(blogId);
        int totalPages = (int) Math.ceil((double) totalReviews / size);

        return new BlogReviewResponseDto(stats, reviews, totalReviews, totalPages);
    }

    // 리뷰 등록 로직
    @Transactional
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

    @Transactional
    public void toggleLike(Long reviewId, BigInteger socialId) {
        User user = userRepository.findBySocialId(socialId);
        // 비관적 락 방지
        BlogReview blogReview = blogReviewRepository.findByBlogReviewId(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        // 좋아요 기록 확인
        Optional<ReviewLike> existingLike = reviewLikeRepository.findByBlogReviewAndUser(blogReview, user);
        if (existingLike.isPresent()) {
            // 이미 좋아요를 누른 상태이면, 좋아요 취소
            reviewLikeRepository.delete(existingLike.get()); // 삭제
            blogReviewRepository.decrementLikes(reviewId); // 좋아요 수 감소
        } else {
            // 좋아요를 누르지 않은 상태이면, 좋아요 추가
            ReviewLike newLike = ReviewLike.builder()
                    .blogReview(blogReview)
                    .user(user)
                    .build();
            reviewLikeRepository.save(newLike); // 새로운 좋아요 저장
            blogReviewRepository.incrementLikes(reviewId); // 좋아요 수 증가
        }
    }

    public Page<ReviewDto> getReviews(Long blogId, BigInteger socialId, int page, int size, String sortBy) {
        User user = userRepository.findBySocialId(socialId);

        Pageable pageable = switch (sortBy) {
            case "recent" -> PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            case "rating-desc" -> PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "rating"));
            case "rating-asc" -> PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "rating"));
            default -> PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "likesCnt"));
        };

        Page<ReviewDto> reviews = blogReviewRepository.findByBlogId(blogId, pageable);

        return reviews.map(review -> {
            BlogReview blogReview = blogReviewRepository.findById(review.getBlogReviewId())
                    .orElseThrow(() -> new IllegalArgumentException("Review not found"));

            boolean isLiked = reviewLikeRepository.existsByBlogReviewAndUser(blogReview, user);

            return ReviewDto.builder()
                    .blogReviewId(review.getBlogReviewId())
                    .rating(review.getRating())
                    .date(review.getDate())
                    .content(review.getContent())
                    .author(review.getAuthor())
                    .profileImage(review.getProfileImage())
                    .likes(review.getLikes())
                    .isLiked(isLiked)
                    .build();
        });
    }
}
