package com.daylily.global.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class CaffeineStateStore implements StateStore {

    private final Cache<String, Boolean> cache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES) // 10분간 유효
            .maximumSize(1_000)                               // 최대 1,000개 저장
            .build();

    private final Cache<String, String> jwtCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES) // JWT 유효 기간 5분
            .maximumSize(1_000)                     // 최대 1,000개 저장
            .build();

    @Override
    public void save(String state, Duration ttl) {
        cache.put(state, Boolean.TRUE);
    }

    @Override
    public void saveJwt(String state, String jwt, Duration ttl) {
        jwtCache.put(state, jwt);
    }

    @Override
    public boolean consume(String state) {
        return cache.asMap().remove(state) != null;
    }

    @Override
    public String consumeJwt(String state) {
        return jwtCache.asMap().remove(state);
    }
}
