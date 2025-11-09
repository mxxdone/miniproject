package com.mxxdone.miniproject.service;

import com.mxxdone.miniproject.config.security.jwt.JwtUtil;
import com.mxxdone.miniproject.domain.User;
import com.mxxdone.miniproject.dto.user.TokenResponseDto;
import com.mxxdone.miniproject.repository.UserRepository;
import com.mxxdone.miniproject.util.CookieUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final RefreshTokenService refreshTokenService;

    /**
     * Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급
     *
     * 전체 흐름:
     * 1. 클라이언트가 보낸 refresh token을 추출하여 유효성 검증
     * 2. 토큰에서 사용자 정보를 추출하고 DB에서 사용자 조회
     * 3. 클라이언트의 토큰과 쿠키에 저장된 토큰을 비교
     * 4. 검증 성공 시 새로운 토큰 발급 및 DB 업데이트
     *
     * @param request HTTP 요청 객체 (헤더에 refresh token이 포함됨)
     * @param response HTTP 응답 객체
     * @return 새로 발급된 Access Token과 Refresh Token
     */
    public TokenResponseDto refresh(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 리프레시 토큰 추출
        String refreshTokenFromCookie = cookieUtil.getCookie(request, "refreshToken")
                .map(Cookie::getValue)
                .orElse(null);

        // 쿠키에 토큰이 없거나 JWT 토큰 유효성 검증 실패 시 예외 처리
        if (refreshTokenFromCookie == null || !jwtUtil.validateToken(refreshTokenFromCookie)) {
            log.error("리프레시 토큰 쿠키가 없거나 유효하지 않습니다. token: {}", refreshTokenFromCookie);
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 토큰에서 사용자 정보 추출
        Claims info = jwtUtil.getUserInfoFromToken(refreshTokenFromCookie);
        String username = info.getSubject();

        // Redis에서 해당 username의 Refresh Token 조회
        String refreshTokenFromRedis = refreshTokenService.findRefreshTokenByUsername(username);

        // Redis에 토큰이 없거나, 쿠키의 토큰과 Redis의 토큰이 일치하지 않으면 예외 처리
        // 로그아웃되거나 다른 곳에서 로그인하여 토큰이 변경된 경우
        if (refreshTokenFromRedis == null || !refreshTokenFromCookie.equals(refreshTokenFromRedis)) {
            log.error("Redis에 리프레시 토큰이 없거나 쿠키와 일치하지 않습니다. Cookie: {}, Redis: {}", refreshTokenFromCookie, refreshTokenFromRedis);
            refreshTokenService.deleteRefreshToken(username);
            throw new IllegalArgumentException("유효하지 않거나 만료된 리프레시 토큰입니다.");
        }

        // 사용자 정보 조회 (새로운 액세스 토큰 발급시 역할 정보 필요)
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("리프레시 토큰에 해당하는 사용자를 찾을 수 없습니다. username: {}", username);
                    return new IllegalArgumentException("사용자를 찾을 수 없습니다.");
                });

        // 검증 종료

        // 새로운 액세스 토큰과 리프레시 토큰 발급
        String newAccessToken = jwtUtil.createAccessToken(user.getUsername(), user.getRole(), user.getNickname());
        String newRefreshToken = jwtUtil.createRefreshToken(user.getUsername());

        // 새로운 리프레시 토큰을 Redis에 저장 (기존 토큰을 덮어쓰기)
        Duration refreshTokenDuration = Duration.ofDays(jwtUtil.getRefreshTokenExpirationDays());
        refreshTokenService.saveRefreshToken(user.getUsername(), newRefreshToken, refreshTokenDuration);

        //새로운 리프레시 토큰으로 쿠키 업데이트
        long refreshTokenMaxAgeSeconds = refreshTokenDuration.toSeconds();
        cookieUtil.addCookie(response, "refreshToken", newRefreshToken, refreshTokenMaxAgeSeconds);

        // 새로운 액세스 토큰만 리턴
        return new TokenResponseDto(newAccessToken);
    }

    // 로그아웃 시 Redis 토큰 삭제
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtil.getCookie(request, "refreshToken")
                .map(Cookie::getValue)
                .orElse(null);

        if (refreshToken != null && jwtUtil.validateToken(refreshToken)) {
            Claims claims = jwtUtil.getUserInfoFromToken(refreshToken);
            String username = claims.getSubject();

            // Redis에서 토큰 삭제
            refreshTokenService.deleteRefreshToken(username);

            // 쿠키 삭제
            cookieUtil.deleteCookie(request, response, "refreshToken");
        }
    }
}
