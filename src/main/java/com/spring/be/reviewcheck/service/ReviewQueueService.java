package com.spring.be.reviewcheck.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.be.reviewcheck.utils.RedisCacheUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ReviewQueueService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisCacheUtil redisCacheUtil;
    private final ObjectMapper objectMapper;

    public ReviewQueueService(RedisTemplate<String, String> redisTemplate, RedisCacheUtil redisCacheUtil, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.redisCacheUtil = redisCacheUtil;
        this.objectMapper = objectMapper;
    }

    // 큐에 작업 추가
    public void enqueueReviewCheckResult(String requestId, String blogUrl) {
        Map<String, String> requestPayload = new HashMap<>();
        requestPayload.put("requestId", requestId);
        requestPayload.put("blogUrl", blogUrl);

        try {
            String payloadJson = objectMapper.writeValueAsString(requestPayload);
            redisTemplate.opsForList().leftPush("reviewQueue", payloadJson);
            System.out.println("Enqueued review check request: " + payloadJson);
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing request payload: " + e.getMessage());
        }
    }

    // 큐에서 작업 처리
    public Map<String, String> processReviewQueue() {
        String payloadJson = redisTemplate.opsForList().rightPop("reviewQueue");
        if (payloadJson != null) {
            try {
                return objectMapper.readValue(payloadJson, new TypeReference<Map<String, String>>() {});
            } catch (JsonProcessingException e) {
                System.err.println("Error parsing request payload JSON: " + e.getMessage());
            }
        }
        return null;
    }
}
