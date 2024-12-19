package com.spring.be.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisCacheUtil {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    // 데이터 캐싱
    public void cacheResult(String key, String jsonValue) {
        try {
            // 2시간 동안 캐싱
            redisTemplate.opsForValue().set(key, jsonValue, 2, TimeUnit.HOURS);
            System.out.println("[Redis] Cached result - Key: " + key + ", Value: " + jsonValue);
        } catch (Exception e) {
            System.err.println("[Redis] Error caching result - Key: " + key + ", Error: " + e.getMessage());
        }
    }

    // 캐싱된 데이터 가져오기 (일반적인 문자열 반환)
    public String getCachedResult(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                System.out.println("[Redis] Cache hit - Key: " + key + ", Value: " + value);
            } else {
                System.out.println("[Redis] Cache miss - Key: " + key);
            }
            return value;
        } catch (Exception e) {
            System.err.println("[Redis] Error retrieving cache - Key: " + key + ", Error: " + e.getMessage());
            return null;
        }
    }

    // 캐싱된 데이터를 제네릭 타입으로 변환하여 가져오기
    public <T> T getCachedResult(String key, Class<T> clazz) {
        try {
            String jsonValue = redisTemplate.opsForValue().get(key);
            if (jsonValue != null) {
                System.out.println("[Redis] Cache hit - Key: " + key + ", Value: " + jsonValue);
                return objectMapper.readValue(jsonValue, clazz); // JSON을 객체로 변환
            } else {
                System.out.println("[Redis] Cache miss - Key: " + key);
                return null;
            }
        } catch (Exception e) {
            System.err.println("[Redis] Error retrieving or parsing cache - Key: " + key + ", Error: " + e.getMessage());
            return null;
        }
    }

    // Redis 연결 확인
    @PostConstruct
    public void testConnection() {
        try {
            if (redisTemplate.getConnectionFactory() != null) {
                redisTemplate.getConnectionFactory().getConnection().ping();
                System.out.println("[Redis] Successfully connected to Redis");
            } else {
                System.err.println("[Redis] ConnectionFactory is null. Unable to connect to Redis.");
            }
        } catch (Exception e) {
            System.err.println("[Redis] Unable to connect to Redis: " + e.getMessage());
        }
    }

    // Redis 연결 정보 로그
    @PostConstruct
    public void logRedisConnectionInfo() {
        System.out.println("[Redis] Host: " + System.getenv("SPRING_REDIS_HOST"));
        System.out.println("[Redis] Port: " + System.getenv("SPRING_REDIS_PORT"));
        testConnection();
    }
}
