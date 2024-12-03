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
import java.util.NoSuchElementException;

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
            // JWT 검증 실패
            return ResponseEntity.status(401).body(null);
        } catch (NoSuchElementException e) {
            // 해당 블로그 ID가 없을 경우
            return ResponseEntity.status(404).body(null);
        }
    }

    @PatchMapping("/like")
    public ResponseEntity<Void> likeReview(@RequestBody ReviewLikeRequest request) {
        blogReviewService.incrementLikes(request.getReviewId());
        return ResponseEntity.noContent().build(); // 204 No Content 응답
    }

    @GetMapping("/{blogId}/reviews")
    public ResponseEntity<Page<ReviewDto>> getReviews(
            @PathVariable Long blogId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "likes") String sortBy
    ) {
        Page<ReviewDto> reviews = blogReviewService.getReviews(blogId, page, size, sortBy);
        return ResponseEntity.ok(reviews);
    }
}
