package com.spring.be.reviewcheck.service;

import com.spring.be.entity.ReviewCheckResult;
import com.spring.be.reviewcheck.dto.ReviewCheckRequest;
import com.spring.be.reviewcheck.repository.ReviewCheckResultRepository;
import com.spring.be.reviewcheck.utils.RedisCacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReviewCheckService {

    @Autowired
    private ReviewCheckResultRepository reviewCheckResultRepository;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private ReviewQueueService reviewQueueService;

    public ReviewCheckResult createReviewCheckResult(ReviewCheckRequest request) {
        String cacheKey = "reviewResult:" + request.getBlogUrl();

        // 먼저 Redis에서 캐시된 결과가 있는지 확인
        ReviewCheckResult cachedResult = redisCacheUtil.getCachedResult(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        // 캐시된 데이터가 없으면 큐에 작업을 추가
        ReviewCheckResult result = new ReviewCheckResult();
        result.setBlogUrl(request.getBlogUrl());
        result.setSummaryTitle("Processing...");
        result.setSummaryText("The Review Analysis is in progress.");
        result.setScore(-1);
        result.setEvidence("Pending");

        return result;
    }
}
