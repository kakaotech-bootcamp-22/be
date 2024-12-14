package com.spring.be.blogReview.controller;

import com.spring.be.blogReview.dto.*;
import com.spring.be.blogReview.service.BlogReviewService;
import com.spring.be.entity.BlogReview;
import com.spring.be.jwt.config.JwtUtils;
import com.spring.be.util.CookieUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.Map;
import java.util.NoSuchElementException;


@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class BlogReviewController {

    private final BlogReviewService blogReviewService;
    private final JwtUtils jwtUtils;

    @GetMapping("{blogId}")
    public ResponseEntity<BlogReviewResponseDto> getBlogReviews(@PathVariable Long blogId, @CookieValue("jwtToken") String jwtToken) {
        // *********** JWT 필터 구현완료시 변경예정 ***********
        try {
            // JWT 검증 및 사용자 정보 추출
            String socialIdString = jwtUtils.getUsernameFromJwtToken(jwtToken);
            BigInteger socialId = new BigInteger(socialIdString);

            return ResponseEntity.ok(blogReviewService.getBlogReviewResponse(blogId, socialId));
        } catch (IllegalArgumentException e) {
            // JWT 검증 실패
            return ResponseEntity.status(401).body(null);
        }
    }

    // 리뷰 등록
    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(
            @RequestBody ReviewRequest request,
            @CookieValue("jwtToken") String jwtToken) {
        // *********** JWT 필터 구현완료시 변경예정 ***********
        try {
            // JWT 검증 및 사용자 정보 추출
            String socialIdString = jwtUtils.getUsernameFromJwtToken(jwtToken);
            BigInteger socialId = new BigInteger(socialIdString);

            // 리뷰 등록
            Long blogReviewId = blogReviewService.saveReview(request, socialId);

            return ResponseEntity.status(201).body(new ReviewResponseDto(blogReviewId));
        } catch (IllegalStateException e) {
            // 이미 리뷰를 등록한 경우
            return ResponseEntity.status(409).body(null);
        } catch (IllegalArgumentException e) {
            // JWT 검증 실패
            return ResponseEntity.status(401).body(null);
        } catch (NoSuchElementException e) {
            // 해당 블로그 ID가 없을 경우
            return ResponseEntity.status(404).body(null);
        }
    }

    // 좋아요 처리
    @PatchMapping("/like")
    public ResponseEntity<Void> likeReview(@RequestBody ReviewLikeRequest request,@CookieValue("jwtToken") String jwtToken) {
        // *********** JWT 필터 구현완료시 변경예정 ***********
        try {
            // JWT 검증 및 사용자 정보 추출
            String socialIdString = jwtUtils.getUsernameFromJwtToken(jwtToken);
            BigInteger socialId = new BigInteger(socialIdString);
            blogReviewService.toggleLike(request.getReviewId(),socialId);
        }
        catch (IllegalArgumentException e) {
            // JWT 검증 실패
            return ResponseEntity.status(401).body(null);
        }
        return ResponseEntity.noContent().build(); // 204 No Content 응답
    }

    @GetMapping("/{blogId}/reviews")
    public ResponseEntity<Page<ReviewDto>> getReviews(
            @PathVariable Long blogId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "likes") String sortBy,
            @CookieValue("jwtToken") String jwtToken) {
        // *********** JWT 필터 구현완료시 변경예정 ***********
        try {
            // JWT 검증 및 사용자 정보 추출
            String socialIdString = jwtUtils.getUsernameFromJwtToken(jwtToken);
            BigInteger socialId = new BigInteger(socialIdString);

            Page<ReviewDto> reviews = blogReviewService.getReviews(blogId,socialId, page, size, sortBy);
            return ResponseEntity.ok(reviews);
        } catch (IllegalArgumentException e) {
            // JWT 검증 실패
            return ResponseEntity.status(401).body(null);
        }
    }
}
