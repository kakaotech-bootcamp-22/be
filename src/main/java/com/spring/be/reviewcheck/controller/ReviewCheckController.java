package com.spring.be.reviewcheck.controller;

import com.spring.be.entity.ReviewCheckResult;
import com.spring.be.reviewcheck.dto.ReviewCheckRequest;
import com.spring.be.reviewcheck.dto.ReviewCheckResponse;
import com.spring.be.reviewcheck.service.ReviewCheckService;
import com.spring.be.reviewcheck.service.ReviewQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/review-check")
@RequiredArgsConstructor
public class ReviewCheckController {

    private final ReviewCheckService reviewCheckService;
    private final ReviewQueueService reviewQueueService;

    /**
     * 사용자 요청 처리: 리뷰 검사 요청을 큐에 추가하고 기본 응답 반환
     */
    @PostMapping
    public ResponseEntity<ReviewCheckResult> createReviewCheck(@RequestBody ReviewCheckRequest request) {
        try {
            // ReviewCheckService에서 검사 요청 처리 및 기본 응답 생성
            ReviewCheckResult reviewCheck = reviewCheckService.createReviewCheckResult(request);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(reviewCheck);
        } catch (Exception e) {
            System.err.println("Error creating review check: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * AI 서버 응답 처리: 분석 결과를 캐싱하고 데이터베이스에 저장
     */
    @PostMapping("/response")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void receiveAIResponse(@RequestBody ReviewCheckResponse response) {
        try {
            // AI 서버의 응답 처리
            reviewCheckService.cachedReviewCheckResult(response.getRequestId(), response);
            System.out.println("Received and processed AI response for requestId: " + response.getRequestId());
        } catch (Exception e) {
            System.err.println("Error processing AI response: " + e.getMessage());
        }
    }

    /**
     * 요청 상태 확인: Redis에서 결과를 조회
     */
    @GetMapping("/status/{requestId}")
    public ResponseEntity<ReviewCheckResult> getReviewCheckResult(@PathVariable String requestId) {
        try {
            ReviewCheckResult result = reviewCheckService.getReviewCheckResult(requestId);
            if (result != null) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            System.err.println("Error retrieving review check result: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
