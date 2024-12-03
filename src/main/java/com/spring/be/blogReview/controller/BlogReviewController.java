package com.spring.be.blogReview.controller;

import com.spring.be.blogReview.dto.*;
import com.spring.be.blogReview.service.BlogReviewService;
import com.spring.be.entity.BlogReview;
import com.spring.be.jwt.config.JwtUtils;
import com.spring.be.util.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.Map;

@RestController
@RequestMapping("/review")
public class BlogReviewController {

    @Autowired
    private BlogReviewService blogReviewService;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("{blogId}")
    public ResponseEntity<BlogReviewResponseDto> getBlogReviews(@PathVariable Long blogId) {
        return ResponseEntity.ok(blogReviewService.getBlogReviewResponse(blogId));
    }

    // 리뷰 등록
    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(
            @RequestBody ReviewRequest request,
            @CookieValue("jwtToken") String jwtToken) {
        try {
            // JWT 검증 및 사용자 정보 추출
            String socialIdString = jwtUtils.getUsernameFromJwtToken(jwtToken);
            BigInteger socialId = new BigInteger(socialIdString);

            // 리뷰 등록
            Long blogReviewId = blogReviewService.saveReview(request, socialId);

            return ResponseEntity.status(201).body(new ReviewResponseDto(blogReviewId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(null);
        }
    }

    @PatchMapping("/like")
    public ResponseEntity<String> likeReview(@RequestBody ReviewLikeRequest request) {
        blogReviewService.incrementLikes(request.getReviewId());
        return ResponseEntity.ok("좋아요 증가");
    }

    @GetMapping("/{blogId}/likes")
    public Page<ReviewDto> getReviewsByLikesCnt(@PathVariable Long blogId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "5") int size) {
        return blogReviewService.getReviewsByLikesCnt(blogId, page, size);
    }

    @GetMapping("/{blogId}/recent")
    public Page<ReviewDto> getReviewsByCreatedAt(@PathVariable Long blogId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "5") int size) {
        return blogReviewService.getReviewsByCreatedAt(blogId, page, size);
    }

    @GetMapping("/{blogId}/rating-desc")
    public Page<ReviewDto> getReviewsByRatingDesc(@PathVariable Long blogId,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "5") int size) {
        return blogReviewService.getReviewsByRatingDesc(blogId, page, size);
    }

    @GetMapping("/{blogId}/rating-asc")
    public Page<ReviewDto> getReviewsByRatingAsc(@PathVariable Long blogId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "5") int size) {
        return blogReviewService.getReviewsByRatingAsc(blogId, page, size);
    }
}
