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
@RequestMapping("/api/review-checks")
public class ReviewCheckController {

    @Autowired
    private ReviewCheckService reviewCheckService;

    @PostMapping
    public ResponseEntity<ReviewCheckResult> createReviewCheck(@RequestBody ReviewCheckRequest request) {
        ReviewCheckResult reviewCheck = reviewCheckService.createReviewCheckResult(request);
        return ResponseEntity.ok(reviewCheck);
    }
}
