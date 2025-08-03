package com.mxxdone.miniproject.config.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret.key}")
    private String secretKey; // application-secret.yml에 설정한값
    private Key key;
    private static final long EXPIRATION_MINUTES = 60; //토큰 유효시간 60분

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // 토큰 생성
    public String createToken(String username) {
        Instant now = Instant.now();
        Instant expiry = now.plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(username) // 사용자 id
                .issuedAt(Date.from(now)) //만료 시간
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

}
