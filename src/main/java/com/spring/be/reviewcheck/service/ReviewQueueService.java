package com.spring.be.reviewcheck.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.be.reviewcheck.dto.ReviewQueuePayloadDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewQueueService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;


    // 큐에 작업 추가
    public void enqueueReviewCheckResult(String requestId, String blogUrl) {
        ReviewQueuePayloadDto payload = new ReviewQueuePayloadDto(requestId, blogUrl);

        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            redisTemplate.opsForList().leftPush("reviewQueue", payloadJson);
            System.out.println("Enqueued review check request: " + payloadJson);
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing request payload: " + e.getMessage());
        }
    }

    // 큐에서 작업 처리
    public ReviewQueuePayloadDto processReviewQueue() {
        String payloadJson = redisTemplate.opsForList().rightPop("reviewQueue");
        if (payloadJson != null) {
            try {
                return objectMapper.readValue(payloadJson, ReviewQueuePayloadDto.class);
            } catch (JsonProcessingException e) {
                System.err.println("Error parsing request payload JSON: " + e.getMessage());
            }
        }
        return null;
    }
}
