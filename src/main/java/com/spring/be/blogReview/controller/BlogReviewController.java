package com.spring.be.blogReview.controller;

import com.spring.be.blogReview.dto.BlogReviewResponseDto;
import com.spring.be.blogReview.dto.ReviewRequest;
import com.spring.be.blogReview.service.BlogReviewService;
import com.spring.be.jwt.config.JwtUtils;
import com.spring.be.util.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@RequestMapping("/review")
public class BlogReviewController {

    @Autowired
    private BlogReviewService blogReviewService;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("{blog_id}")
    public ResponseEntity<BlogReviewResponseDto> getBlogReviews(@PathVariable Long blog_id) {
        return ResponseEntity.ok(blogReviewService.getBlogReviewResponse(blog_id));
    }

    // 리뷰 등록
    @PostMapping
    public ResponseEntity<String> createReview(
            @RequestBody ReviewRequest request,
            @RequestHeader("Cookie") String cookieHeader
    ) {
        try {
            // 쿠키에서 JWT 추출
            String token = CookieUtils.extractTokenFromCookie(cookieHeader, "jwtToken");

            // JWT 검증 및 사용자 정보 추출
            String socialIdString = jwtUtils.getUsernameFromJwtToken(token);
            BigInteger socialId = new BigInteger(socialIdString);

            // 리뷰 등록
            blogReviewService.saveReview(request, socialId);

            return ResponseEntity.status(201).body("리뷰가 성공적으로 등록되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
