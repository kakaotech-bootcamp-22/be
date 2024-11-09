package com.spring.be.reviewcheck.controller;

import com.spring.be.entity.ReviewCheckResult;
import com.spring.be.reviewcheck.dto.ReviewCheckRequest;
import com.spring.be.reviewcheck.service.ReviewCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/review-check")
public class ReviewCheckController {

    @Autowired
    private ReviewCheckService reviewCheckService;

    // 사용자 요청 처리: 리뷰 검사 요청을 큐에 추가하고 기본 응답 반환
    @PostMapping
    public ResponseEntity<ReviewCheckResult> createReviewCheck(@RequestBody ReviewCheckRequest request) {
        ReviewCheckResult reviewCheck = reviewCheckService.createReviewCheckResult(request);
        return ResponseEntity.ok(reviewCheck);
    }

    // AI 서버 응답 처리: 분석 결과를 캐싱하고 데이터베이스에 저장
    @PostMapping("/response")
    public ResponseEntity<String> receiveAIResponse(@RequestBody ReviewCheckResult result) {
        // AI 서버의 응답을 캐시 및 데이터베이스에 저장
        reviewCheckService.cachedReviewCheckResult(result.getRequestId(), result);
        return ResponseEntity.ok("AI response received and processed.");
    }
}
