package com.spring.be.reviewcheck.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.be.entity.ReviewCheckResult;
import com.spring.be.reviewcheck.dto.ReviewCheckRequestDto;
import com.spring.be.reviewcheck.dto.ReviewCheckResponseDto;
import com.spring.be.reviewcheck.dto.ReviewCheckResultDto;
import com.spring.be.reviewcheck.repository.ReviewCheckResultRepository;
import com.spring.be.reviewcheck.utils.RedisCacheUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewCheckService {

    private final ReviewCheckResultRepository reviewCheckResultRepository;
    private final RedisCacheUtil redisCacheUtil;
    private final ObjectMapper objectMapper;
    private final ReviewQueueService reviewQueueService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.server.url}")
    private String aiServerUrl;

    // 검사 요청 생성
    public ReviewCheckResultDto createReviewCheckResult(ReviewCheckRequestDto request) {
        String blogId = request.getBlogUrl();
        String requestId = UUID.randomUUID().toString();
        String cacheKey = "reviewResult:" + requestId;

        // Redis에서 캐시된 결과 확인
        String cachedJson = redisCacheUtil.getCachedResult(cacheKey);
        if (cachedJson != null) {
            try {
                ReviewCheckResult cachedResult = objectMapper.readValue(cachedJson, ReviewCheckResult.class);
                return toResultDto(cachedResult, "Completed");
            } catch (JsonProcessingException e) {
                System.err.println("Error parsing cached json: " + e.getMessage());
            }
        }

        // 큐에 작업 추가
        reviewQueueService.enqueueReviewCheckResult(requestId, blogId);

        // AI 서버로 요청 전송
        sendRequestToAIServer(requestId, blogId);

        // 기본 응답 생성
        ReviewCheckResult result = new ReviewCheckResult();
        result.setRequestId(requestId);
        result.setBlogUrl(blogId);
        result.setSummaryTitle("Processing...");
        result.setSummaryText("The Review Analysis is in progress.");
        result.setScore(-1);
        result.setEvidence("Pending");

        // Redis에 기본 응답 캐싱
        cacheResultToRedis(result);

        return toResultDto(result, "Processing");
    }

    // AI 서버에 검사 요청 전송
    private void sendRequestToAIServer(String requestId, String blogUrl) {
        try {
            // 요청 데이터 생성
            ReviewCheckRequestDto requestDto = new ReviewCheckRequestDto();
            requestDto.setBlogUrl(blogUrl);

            // HTTP 요청 생성
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ReviewCheckRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);

            // AI 서버에 요청 전송
            restTemplate.postForEntity(aiServerUrl + "/review-check", requestEntity, Void.class);
            System.out.println("Request to AI Server: " + requestId);
        } catch (Exception e) {
            System.err.println("Error sending request to AI server: " + e.getMessage());
        }
    }


    // 결과 저장 및 업데이트
    public void cachedReviewCheckResult(String requestId, ReviewCheckResponseDto responseDto) {
        String cacheKey = "reviewResult:" + requestId;

        try {
            // Redis에 AI 응답 데이터 저장
            redisCacheUtil.cacheResult(cacheKey, objectMapper.writeValueAsString(responseDto));

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

    // Redis에 기본 응답 저장
    private void cacheResultToRedis(ReviewCheckResult result) {
        try {
            redisCacheUtil.cacheResult("reviewResult:" + result.getRequestId(), objectMapper.writeValueAsString(result));
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing result for caching: " + e.getMessage());
        }
    }

    // 기존 DB 데이터 업데이트
    private void updateExistingResult(ReviewCheckResult existingResult, ReviewCheckResult newResult) {
        existingResult.setSummaryTitle(newResult.getSummaryTitle());
        existingResult.setSummaryText(newResult.getSummaryText());
        existingResult.setScore(newResult.getScore());
        existingResult.setEvidence(newResult.getEvidence());
        reviewCheckResultRepository.save(existingResult);
    }

    // 새 데이터 DB 저장
    private void saveNewResult(ReviewCheckResult newResult) {
        reviewCheckResultRepository.save(newResult);
    }

    // 결과 조회
    public ReviewCheckResultDto getReviewCheckResult(String requestId) {
        String cacheKey = "reviewResult:" + requestId;

        // Redis에서 결과 조회
        String cachedJson = redisCacheUtil.getCachedResult(cacheKey);
        if (cachedJson != null) {
            try {
                ReviewCheckResult cachedResult = objectMapper.readValue(cachedJson, ReviewCheckResult.class);
                return toResultDto(cachedResult, "Completed");
            } catch (JsonProcessingException e) {
                System.err.println("Error parsing cached JSON for requestId " + requestId + ": " + e.getMessage());
            }
        }

        // 결과가 없는 경우 null 반환
        System.out.println("No cached result found for requestId: " + requestId);
        return null;
    }

    // ReviewCheckResult Entity를 ReviewCheckResultDto로 변환
    private ReviewCheckResultDto toResultDto(ReviewCheckResult entity, String status) {
        return new ReviewCheckResultDto(
                entity.getRequestId(),
                entity.getBlogUrl(),
                entity.getSummaryTitle(),
                entity.getSummaryText(),
                entity.getScore(),
                entity.getEvidence(),
                status
        );
    }
}
