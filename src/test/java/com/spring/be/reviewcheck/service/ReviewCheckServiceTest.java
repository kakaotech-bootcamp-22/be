package com.spring.be.reviewcheck.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spring.be.entity.ReviewCheckResult;
import com.spring.be.reviewcheck.dto.ReviewCheckRequest;
import com.spring.be.reviewcheck.repository.ReviewCheckResultRepository;
import com.spring.be.reviewcheck.utils.RedisCacheUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
//import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewCheckServiceTest {

    @Mock
    private ReviewCheckResultRepository reviewCheckResultRepository;

    @Mock
    private RedisCacheUtil redisCacheUtil;

    @Mock
    private ReviewQueueService reviewQueueService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ReviewCheckService reviewCheckService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void createReviewCheckResult_whenCacheExits() throws JsonProcessingException {
        // Given
        String blogUrl = "https://blog.naver.com/example/123456789";
        String requestId = UUID.randomUUID().toString();
        String cacheKey = "reviewResult:" + blogUrl;
        ReviewCheckRequest request = new ReviewCheckRequest();
        request.setBlogUrl(blogUrl);

        ReviewCheckResult cachedResult = new ReviewCheckResult();
        cachedResult.setRequestId(requestId);
        cachedResult.setBlogUrl(blogUrl);
        cachedResult.setSummaryTitle("Cached Title");
        cachedResult.setSummaryText("Cached Text");
        cachedResult.setScore(100);
        cachedResult.setEvidence("Cached Evidence");

        String cachedJson = "{\"blogUrl\":\"http://example.com/review\",\"summaryTitle\":\"Cached Title\",\"summaryText\":\"Cached content\",\"score\":100,\"evidence\":\"Cached evidence\"}";

        // Mock Redis 캐시에서 JSON 데이터가 존재하는 상황
        when(redisCacheUtil.getCachedResult(cacheKey)).thenReturn(cachedJson);
        when(objectMapper.readValue(cachedJson, ReviewCheckResult.class)).thenReturn(cachedResult);

        // When
        ReviewCheckResult result = reviewCheckService.createReviewCheckResult(request);

        // Then
        assertNotNull(result);
        assertEquals("Cached Title", result.getSummaryTitle());
        assertEquals("Cached Text", result.getSummaryText());
        assertEquals(100, result.getScore());
        verify(redisCacheUtil, times(1)).getCachedResult(cacheKey);
        verify(reviewQueueService, times(0)).enqueueReviewCheckResult(requestId, blogUrl);
    }

    @Test
    void createReviewCheckResult_WhenCacheDoesNotExist() {
        // Given
        String blogUrl = "https://blog.naver.com/example/123456789";
        String cacheKey = "reviewResult:" + blogUrl;
        String requestId = UUID.randomUUID().toString();
        ReviewCheckRequest request = new ReviewCheckRequest();
        request.setBlogUrl(blogUrl);

        // Mock Redis 캐시에서 데이터가 존재하지 않는 상황
        when(redisCacheUtil.getCachedResult(cacheKey)).thenReturn(null);

        // When
        ReviewCheckResult result = reviewCheckService.createReviewCheckResult(request);

        // Then
        assertNotNull(result);
        assertEquals("Processing...", result.getSummaryTitle());
        assertEquals("The Review Analysis is in progress.", result.getSummaryText());
        assertEquals(-1, result.getScore());
        verify(redisCacheUtil, times(1)).getCachedResult(cacheKey);
        verify(reviewQueueService, times(1)).enqueueReviewCheckResult(requestId, blogUrl);
    }

    @Test
    void cachedReviewCheckResult() throws JsonProcessingException {
        // Given
        String blogUrl = "https://blog.naver.com/example/123456789";
        ReviewCheckResult result = new ReviewCheckResult();
        result.setBlogUrl(blogUrl);
        result.setSummaryTitle("New Title");
        result.setSummaryText("New Text");
        result.setScore(50);
        result.setEvidence("New Evidence");

        String jsonResult = "{\"blogUrl\":\"http://example.com/review\",\"summaryTitle\":\"New Title\",\"summaryText\":\"New content\",\"score\":85,\"evidence\":\"New evidence\"}";
        String cacheKey = "reviewResult:" + blogUrl;

        // Mock ObjectMapper 에서 JSON 문자열로 변환
        when(objectMapper.writeValueAsString(result)).thenReturn(jsonResult);

        // When
        reviewCheckService.cachedReviewCheckResult(blogUrl, result);

        // Then
        verify(redisCacheUtil, times(1)).cacheResult(cacheKey, jsonResult);
    }
}