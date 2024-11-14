package com.spring.be.reviewcheck.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.be.entity.ReviewCheckResult;
import com.spring.be.reviewcheck.repository.ReviewCheckResultRepository;
import com.spring.be.reviewcheck.utils.RedisCacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ReviewQueueService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisCacheUtil redisCacheUtil;
    private final ReviewCheckResultRepository reviewCheckResultRepository;
    private final RestTemplate restTemplate;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    public ObjectMapper objectMapper;

    public ReviewQueueService(RedisTemplate<String, String> redisTemplate, RedisCacheUtil redisCacheUtil,
                              ReviewCheckResultRepository reviewCheckResultRepository, RestTemplate restTemplate,
                              ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.redisCacheUtil = redisCacheUtil;
        this.reviewCheckResultRepository = reviewCheckResultRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public void enqueueReviewCheckResult(String requestId, String blogUrl) {
        Map<String, String> requestPayload = new HashMap<>();
        requestPayload.put("requestId", requestId);
        requestPayload.put("blogUrl", blogUrl);

        try {
            String payloadJson = objectMapper.writeValueAsString(requestPayload);
            redisTemplate.opsForList().leftPush("reviewQueue", payloadJson);
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing request payload: " + e.getMessage());
        }
    }

    public void processReviewQueue() {
        String payloadJson = redisTemplate.opsForList().rightPop("reviewQueue");

        if (payloadJson != null) {
            try {
                Map<String, String> payload = objectMapper.readValue(payloadJson, new TypeReference<Map<String, String>>() {});
                String requestId = payload.get("requestId");
                String blogUrl = payload.get("blogUrl");

                // AI 서버에 요청 전송 및 결과 수신
                ReviewCheckResult result;

                try {
                    result = restTemplate.postForObject(aiServerUrl, payload, ReviewCheckResult.class);

                    if (result == null) {
                        throw new RestClientException("Empty response from AI server");
                    }
                    result.setRequestId(requestId);
                } catch (RestClientException e) {
                    result = createDefaultReviewCheckResult(blogUrl, "Error: Could not process the review");
                    result.setRequestId(requestId);
                    System.err.println("Error: " + e.getMessage());
                }

                // Redis에 검사 결과 캐싱
                try {
                    String jsonResult = objectMapper.writeValueAsString(result);
                    String cacheKey = "reviewResult:" + requestId;
                    redisCacheUtil.cacheResult(cacheKey, jsonResult);
                } catch (JsonProcessingException e) {
                    System.err.println("Error parsing request payload JSON: " + e.getMessage());
                }

                // 데이터베이스에 결과 저장
                // reviewCheckResultRepository.save(result);
            } catch (JsonProcessingException e) {
                System.err.println("Error parsing request payload JSON: " + e.getMessage());
            }
        }
    }

    private ReviewCheckResult createDefaultReviewCheckResult(String blogUrl, String errorMessage) {
        ReviewCheckResult result = new ReviewCheckResult();
        result.setBlogUrl(blogUrl);
        result.setSummaryTitle("error");
        result.setSummaryText(errorMessage);
        result.setScore(-1);
        result.setEvidence("error result");
        return result;
    }


}
