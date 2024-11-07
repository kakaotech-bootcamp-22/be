package com.spring.be.reviewcheck.utils;

import com.spring.be.entity.ReviewCheckResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisCacheUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void cacheResult(String key, ReviewCheckResult result) {
        // 2시간 동안 캐싱
        redisTemplate.opsForValue().set(key, result, 2, TimeUnit.HOURS);
    }

    public ReviewCheckResult getCachedResult(String key) {
        return (ReviewCheckResult) redisTemplate.opsForValue().get(key);
    }
}
