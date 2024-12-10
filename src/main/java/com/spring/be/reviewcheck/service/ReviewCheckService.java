package com.spring.be.reviewcheck.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.be.entity.ReviewCheckResult;
import com.spring.be.reviewcheck.dto.ReviewCheckRequest;
import com.spring.be.reviewcheck.repository.ReviewCheckResultRepository;
import com.spring.be.reviewcheck.utils.RedisCacheUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ReviewCheckService {

    @Value("${ai.server.url}") // Parameter Store에서 가져오는 값
    private String aiServerUrl;

   private final ReviewCheckResultRepository reviewCheckResultRepository;
   private final RedisCacheUtil redisCacheUtil;
   private final ObjectMapper objectMapper;
   private final ReviewQueueService reviewQueueService;


    // 결과 생성
    public ReviewCheckResult createReviewCheckResult(ReviewCheckRequest request) {
        String blogId = request.getBlogUrl();
        String requestId = UUID.randomUUID().toString();
        String cacheKey = "reviewResult:" + requestId;

        // 먼저 Redis에서 캐시된 결과가 있는지 확인
        String cachedJson = redisCacheUtil.getCachedResult(cacheKey);
        if (cachedJson != null) {
            try {
                System.out.println("Cached data found: " + cachedJson);
                return objectMapper.readValue(cachedJson, ReviewCheckResult.class);
            } catch (JsonProcessingException e) {
                System.err.println("Error parsing cached json: " + e.getMessage());
            }
        } else {
            System.out.println("Cached data not found: " + cacheKey);
        }

        // 캐시된 데이터가 없으면 큐에 작업을 추가하고 기본 응답 반환
        reviewQueueService.enqueueReviewCheckResult(requestId, blogId);

        // 기본 응답 생성
        ReviewCheckResult result = new ReviewCheckResult();
        result.setRequestId(requestId);
        result.setBlogUrl(blogId);
        result.setSummaryTitle("Processing...");
        result.setSummaryText("The Review Analysis is in progress.");
        result.setScore(-1);
        result.setEvidence("Pending");

        // Redis에 초기 상태 (기본 응답) 캐싱
        try {
            String resultJson = objectMapper.writeValueAsString(result);
            redisCacheUtil.cacheResult(cacheKey, resultJson);
            System.out.println("Cached initial result for key: " + cacheKey);
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing initial result for caching: " + e.getMessage());
        }

        // AI 서버로 POST 검사 요청 전송
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> aiRequest = new HashMap<>();
            aiRequest.put("requestId", requestId);
            aiRequest.put("blogUrl", blogId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(aiRequest, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(aiServerUrl + "/review-check", entity, String.class);
            System.out.println("Sent AI review check request. Response: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Error sending AI review check request: " + e.getMessage());
        }

        return result;
    }

    // 결과 저장
    public void cachedReviewCheckResult(String requestId, ReviewCheckResult result) {
        try {
            String jsonResult = objectMapper.writeValueAsString(result);
            String cacheKey = "reviewResult:" + requestId;
            redisCacheUtil.cacheResult(cacheKey, jsonResult);
        } catch (JsonProcessingException e) {
            System.err.println("Error converting result to JSON: " + e.getMessage());
        }
    }


    // 결과 조회
    public ReviewCheckResult getReviewCheckResult(String requestId) {
        String cacheKey = "reviewResult:" + requestId;

        // Redis 캐시에서 조회
        String cachedJson = redisCacheUtil.getCachedResult(cacheKey);
        if (cachedJson != null) {
            try {
                return objectMapper.readValue(cachedJson, ReviewCheckResult.class);
            } catch (JsonProcessingException e) {
                System.err.println("Error parsing cached JSON: " + e.getMessage());
            }
        }

        return null;
    }

}
