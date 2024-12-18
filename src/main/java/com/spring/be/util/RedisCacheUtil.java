package com.spring.be.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisCacheUtil {

    @Autowired
    private final RedisTemplate<String, String> redisTemplate;

    public RedisCacheUtil(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void cacheResult(String key, String jsonValue) {
        try {
            // 2시간 동안 캐싱
            redisTemplate.opsForValue().set(key, jsonValue, 2, TimeUnit.HOURS);
            System.out.println("[Redis] Cached result - Key: " + key + ", Value: " + jsonValue);
        } catch (Exception e) {
            System.err.println("[Redis] Error caching result - Key: " + key + ", Error: " + e.getMessage());
        }
    }

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
}
