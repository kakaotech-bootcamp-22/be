package com.spring.be.reviewcheck.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.be.entity.ReviewCheckResult;
import com.spring.be.reviewcheck.dto.ReviewCheckResultDto;
import com.spring.be.util.RedisCacheUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

class ReviewCheckServiceTest {

    @Mock
    private RedisCacheUtil redisCacheUtil;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ReviewCheckService reviewCheckService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getReviewCheckResult_WhenCacheExists_ShouldReturnResult() throws JsonProcessingException {
        // Given
        String requestId = "123e4567-e89b-12d3-a456-426614174000";
        String cacheKey = "reviewResult:" + requestId;
        String cachedJson = "{\"requestId\":\"123e4567-e89b-12d3-a456-426614174000\",\"blogUrl\":\"https://blog.naver.com/kakao_food_fighter/1038913\",\"summaryTitle\":\"Test\",\"summaryText\":\"Test summary\",\"score\":100,\"evidence\":\"Test evidence\"}";

        ReviewCheckResult expectedResult = new ReviewCheckResult();
        expectedResult.setRequestId(requestId);
        expectedResult.setBlogUrl("https://blog.naver.com/kakao_food_fighter/1038913");
        expectedResult.setSummaryTitle("Test");
        expectedResult.setSummaryText("Test summary");
        expectedResult.setScore(100);
        expectedResult.setEvidence("Test evidence");

        when(redisCacheUtil.getCachedResult(cacheKey)).thenReturn(cachedJson);
        when(objectMapper.readValue(cachedJson, ReviewCheckResult.class)).thenReturn(expectedResult);

        // When
        ReviewCheckResultDto actualResult = reviewCheckService.getReviewCheckResult(requestId);

        // Then
        assertNotNull(actualResult);
        assertEquals(expectedResult.getRequestId(), actualResult.getRequestId());
        assertEquals(expectedResult.getBlogUrl(), actualResult.getBlogUrl());
        assertEquals(expectedResult.getSummaryTitle(), actualResult.getSummaryTitle());
        assertEquals(expectedResult.getSummaryText(), actualResult.getSummaryText());
        assertEquals(expectedResult.getScore(), actualResult.getScore());
        assertEquals(expectedResult.getEvidence(), actualResult.getEvidence());
    }

    @Test
    void getReviewCheckResult_WhenCacheDoesNotExist_ShouldReturnNull() {
        // Given
        String requestId = "123e4567-e89b-12d3-a456-426614174000";
        String cacheKey = "reviewResult:" + requestId;

        when(redisCacheUtil.getCachedResult(cacheKey)).thenReturn(null);

        // When
        ReviewCheckResultDto result = reviewCheckService.getReviewCheckResult(requestId);

        // Then
        assertNull(result);
    }

    @Test
    void getReviewCheckResult_WhenJsonParsingFails_ShouldReturnNull() throws JsonProcessingException {
        // Given
        String requestId = "123e4567-e89b-12d3-a456-426614174000";
        String cacheKey = "reviewResult:" + requestId;
        String cachedJson = "{\"invalidJson";

        when(redisCacheUtil.getCachedResult(cacheKey)).thenReturn(cachedJson);
        when(objectMapper.readValue(cachedJson, ReviewCheckResult.class)).thenThrow(JsonProcessingException.class);

        // When
        ReviewCheckResultDto result = reviewCheckService.getReviewCheckResult(requestId);

        // Then
        assertNull(result);
        verify(objectMapper, times(1)).readValue(cachedJson, ReviewCheckResult.class);
    }
}
