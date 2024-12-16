package com.spring.be.reviewcheck.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.be.entity.ReviewCheckResult;
import com.spring.be.reviewcheck.dto.ReviewCheckRequestDto;
import com.spring.be.reviewcheck.dto.ReviewCheckResponseDto;
import com.spring.be.reviewcheck.repository.ReviewCheckResultRepository;
import com.spring.be.reviewcheck.utils.RedisCacheUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewCheckService {

    private final ReviewCheckResultRepository reviewCheckResultRepository;
    private final RedisCacheUtil redisCacheUtil;
    private final ObjectMapper objectMapper;
    private final ReviewQueueService reviewQueueService;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    // 검사 요청 생성
    public ReviewCheckResult createReviewCheckResult(ReviewCheckRequestDto request) {
        String blogId = request.getBlogUrl();
        String requestId = UUID.randomUUID().toString();
        String cacheKey = "reviewResult:" + requestId;

        // Redis에서 캐시된 결과 확인
        String cachedJson = redisCacheUtil.getCachedResult(cacheKey);
        if (cachedJson != null) {
            try {
                return objectMapper.readValue(cachedJson, ReviewCheckResult.class);
            } catch (JsonProcessingException e) {
                System.err.println("Error parsing cached json: " + e.getMessage());
            }
        }

        // 큐에 작업 추가
        reviewQueueService.enqueueReviewCheckResult(requestId, blogId);

        // 기본 응답 생성
        ReviewCheckResult result = new ReviewCheckResult();
        result.setRequestId(requestId);
        result.setBlogUrl(blogId);
        result.setSummaryTitle("Processing...");
        result.setSummaryText("The Review Analysis is in progress.");
        result.setScore(-1);
        result.setEvidence("Pending");

        // Redis에 기본 응답 캐싱
        try {
            String resultJson = objectMapper.writeValueAsString(result);
            redisCacheUtil.cacheResult(cacheKey, resultJson);
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing initial result for caching: " + e.getMessage());
        }

        return result;
    }

    // 결과 저장 및 업데이트
    public void cachedReviewCheckResult(String requestId, ReviewCheckResponseDto responseDto) {
        String cacheKey = "reviewResult:" + requestId;

        try {
            // Redis에 AI 응답 데이터 저장
            String jsonResult = objectMapper.writeValueAsString(responseDto);
            redisCacheUtil.cacheResult(cacheKey, jsonResult);

            ReviewCheckResult result = new ReviewCheckResult();
            result.setRequestId(responseDto.getRequestId());
            result.setBlogUrl(responseDto.getBlogUrl());
            result.setSummaryTitle(responseDto.getSummaryTitle());
            result.setSummaryText(responseDto.getSummaryText());
            result.setScore(responseDto.getScore());
            result.setEvidence(responseDto.getEvidence());

            // user 필드 확인
            if (result.getUser() == null) {
                System.out.println("No user associated with this result. Skipping database update.");
                return; // 데이터베이스 저장 생략
            }

            // DB 저장
            ReviewCheckResult existingResult = reviewCheckResultRepository.findByRequestId(requestId);
            if (existingResult != null) {
                updateExistingResult(existingResult, result);
            } else {
                saveNewResult(result);
            }
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing response result: " + e.getMessage());
        }
    }

    private void updateExistingResult(ReviewCheckResult existingResult, ReviewCheckResult newResult) {
        existingResult.setSummaryTitle(newResult.getSummaryTitle());
        existingResult.setSummaryText(newResult.getSummaryText());
        existingResult.setScore(newResult.getScore());
        existingResult.setEvidence(newResult.getEvidence());
        reviewCheckResultRepository.save(existingResult);
    }

    private void saveNewResult(ReviewCheckResult newResult) {
        reviewCheckResultRepository.save(newResult);
    }

    public ReviewCheckResult getReviewCheckResult(String requestId) {
        String cacheKey = "reviewResult:" + requestId;

        // Redis에서 결과 조회
        String cachedJson = redisCacheUtil.getCachedResult(cacheKey);
        if (cachedJson != null) {
            try {
                return objectMapper.readValue(cachedJson, ReviewCheckResult.class);
            } catch (JsonProcessingException e) {
                System.err.println("Error parsing cached JSON for requestId " + requestId + ": " + e.getMessage());
            }
        }

        // 결과가 없는 경우 null 반환
        System.out.println("No cached result found for requestId: " + requestId);
        return null;
    }
}
