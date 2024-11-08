package com.spring.be.reviewcheck.utils;

import com.spring.be.entity.ReviewCheckResult;
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
        // 2시간 동안 캐싱
        redisTemplate.opsForValue().set(key, jsonValue, 2, TimeUnit.HOURS);
    }

    public String getCachedResult(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}
