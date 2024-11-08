package com.spring.be.reviewcheck.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    @Autowired
    private ObjectMapper jacksonObjectMapper;

    public ReviewCheckResult createReviewCheckResult(ReviewCheckRequest request) {
        String cacheKey = "reviewResult:" + request.getBlogUrl();

        // 먼저 Redis에서 캐시된 결과가 있는지 확인
        String cachedJson = redisCacheUtil.getCachedResult(cacheKey);
        if (cachedJson != null) {
            try {
                return jacksonObjectMapper.readValue(cachedJson, ReviewCheckResult.class);
            } catch (JsonProcessingException e) {
                System.err.println("Error parsing cached json: " + e.getMessage());
            }
        }

        // 캐시된 데이터가 없으면 큐에 작업을 추가하고 기본 응답 반환
        reviewQueueService.enqueueReviewCheckResult(request.getBlogUrl());

        // 캐시된 데이터가 없으면 큐에 작업을 추가
        ReviewCheckResult result = new ReviewCheckResult();
        result.setBlogUrl(request.getBlogUrl());
        result.setSummaryTitle("Processing...");
        result.setSummaryText("The Review Analysis is in progress.");
        result.setScore(-1);
        result.setEvidence("Pending");

        return result;
    }

    // 결과 저장
    public void cachedReviewCheckResult(String blogUrl, ReviewCheckResult result) {
        try {
            String jsonResult = jacksonObjectMapper.writeValueAsString(result);
            String cacheKey = "reviewResult:" + blogUrl;
            redisCacheUtil.cacheResult(cacheKey, jsonResult);
        } catch (JsonProcessingException e) {
            System.err.println("Error converting result to JSON: " + e.getMessage());
        }
    }
}
