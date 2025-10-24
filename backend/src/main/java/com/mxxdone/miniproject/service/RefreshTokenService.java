package com.mxxdone.miniproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    // Redis에 리프레시 토큰 저장 (Key: username, Valeu: refreshToken)
    public void saveRefreshToken(String username, String refreshToken, Duration duration) {
        // ValueOperations: String 자료구조 조작을 위한 API
        ValueOperations<String, String> valueOperation = redisTemplate.opsForValue();
        // 저장시 refreshToken 접두사 사용하여 구분
        valueOperation.set("refreshToken:" + username, refreshToken, duration);
    }

    // Redis에서 username으로 리프레시 토큰 조회
    public String findRefreshTokenByUsername(String username) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get("refreshToken:" + username);
    }

    // Redis에서 리프레시 토큰 삭제
    public void deleteRefreshToken(String username) {
        redisTemplate.delete("refreshToken:" + username);
    }
}
