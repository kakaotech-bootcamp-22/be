package com.spring.be.reviewcheck.controller;

import com.spring.be.entity.ReviewCheckResult;
import com.spring.be.reviewcheck.dto.ReviewCheckRequest;
import com.spring.be.reviewcheck.service.ReviewCheckService;
import org.hibernate.annotations.NotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/review-check")
public class ReviewCheckController {

    @Autowired
    private ReviewCheckService reviewCheckService;

    // 사용자 요청 처리: 리뷰 검사 요청을 비동기 큐에 추가하고 기본 응답 반환
    @PostMapping
    public ResponseEntity<ReviewCheckResult> createReviewCheck(@RequestBody ReviewCheckRequest request) {
        ReviewCheckResult reviewCheck = reviewCheckService.createReviewCheckResult(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(reviewCheck);
    }

    // AI 서버 응답 처리: 분석 결과를 캐싱하고 데이터베이스에 저장
    @PostMapping("/response")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void receiveAIResponse(@RequestBody ReviewCheckResult result) {
        // AI 서버의 응답을 캐시 및 데이터베이스에 저장
        reviewCheckService.cachedReviewCheckResult(result.getRequestId(), result);
    }

    @GetMapping("/status/{requestId}")
    public ResponseEntity<ReviewCheckResult> getReviewCheckResult(@PathVariable String requestId) {
        ReviewCheckResult result = reviewCheckService.getReviewCheckResult(requestId);
        if (requestId != null) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
