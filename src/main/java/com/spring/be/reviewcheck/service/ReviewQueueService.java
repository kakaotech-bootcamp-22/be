package com.spring.be.reviewcheck.service;

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

    @Autowired
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private ReviewCheckResultRepository reviewCheckResultRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    public ReviewQueueService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void enqueueReviewCheckResult(String blogUrl) {
        redisTemplate.opsForList().leftPush("reviewQueue", blogUrl);
    }

    public void processReviewQueue() {
        String blogUrl = redisTemplate.opsForList().rightPop("reviewQueue");
        if (blogUrl != null) {
            // AI 서버와 통신하여 리뷰 검사 결과 가져오기
            ReviewCheckResult result;
            try {
                Map<String, String> requestPayload = new HashMap<>();
                requestPayload.put("blogUrl", blogUrl);

                result = restTemplate.postForObject(aiServerUrl, requestPayload, ReviewCheckResult.class);

                if (result != null) {
                    throw new RestClientException("Empty response from AI server");
                }
            } catch (RestClientException e) {
                result = createDefaultReviewCheckResult(blogUrl, "Error: Could not process the review");
                System.err.println("Error: " + e.getMessage());
            }

            // Redis에 결과를 캐싱하고 데이터베이스에 저장
            String cacheKey = "reviewResult:" + blogUrl;
            redisCacheUtil.cacheResult(cacheKey, result);
            reviewCheckResultRepository.save(result);
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
