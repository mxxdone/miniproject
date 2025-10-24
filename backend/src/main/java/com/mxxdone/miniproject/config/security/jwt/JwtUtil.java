package com.mxxdone.miniproject.config.security.jwt;

import com.mxxdone.miniproject.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret.key}")
    private String secretKey; // application-secret.yml에 설정한값
    private SecretKey key;
    private static final long ACCESS_TOKEN_EXPIRATION_MINUTES  = 15; //토큰 유효시간 15분
    private static final long REFRESH_TOKEN_EXPIRATION_DAYS  = 7;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // 액세스 토큰 생성 (인증, 인가)
    public String createAccessToken(String username, Role role) {
        Instant now = Instant.now();
        Instant expiry = now.plus(ACCESS_TOKEN_EXPIRATION_MINUTES, ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(username) // 사용자 id
                .claim("auth", role.getKey()) // auth"라는 이름으로 권한 정보 추가
                .issuedAt(Date.from(now)) //만료 시간
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    // 리프레시 토큰 생성
    public String createRefreshToken(String username) {
        Instant now = Instant.now();
        Instant expiry = now.plus(REFRESH_TOKEN_EXPIRATION_DAYS, ChronoUnit.DAYS);

        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    // Request Header에서 토큰 정보 가져오기
    public String getTokenFromRequest(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage(), e);
        }
        return false;
    }

    public Claims getUserInfoFromToken(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    // 리프레시 토큰 만료일 가져오기
    public long getRefreshTokenExpirationDays() {
        return REFRESH_TOKEN_EXPIRATION_DAYS;
    }
}
