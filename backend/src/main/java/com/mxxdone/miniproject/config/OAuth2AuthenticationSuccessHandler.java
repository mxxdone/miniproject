package com.mxxdone.miniproject.config;

import com.mxxdone.miniproject.config.security.PrincipalDetails;
import com.mxxdone.miniproject.config.security.jwt.JwtUtil;
import com.mxxdone.miniproject.domain.User;
import com.mxxdone.miniproject.service.RefreshTokenService;
import com.mxxdone.miniproject.util.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtil cookieUtil;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("====OAuth2.0 Login Success====");

        // 인증된 사용자 정보 가져오기
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        User user = principalDetails.getUser();

        // Access Token 생성
        String accessToken = jwtUtil.createAccessToken(user.getUsername(), user.getRole(), user.getNickname());

        // ===== Refresh Token =====
        // 생성 및 처리
        String refreshToken = jwtUtil.createRefreshToken(user.getUsername());

        // Redis에 저장
        Duration refreshTokenDuration = Duration.ofDays(jwtUtil.getRefreshTokenExpirationDays());

        try {
            refreshTokenService.saveRefreshToken(user.getUsername(), refreshToken, refreshTokenDuration);
        } catch (Exception e) {
            log.error("로그인 성공 후 Redis에 리프레시 토큰 저장 실패 (username: {})", user.getUsername(), e);
            throw new IllegalStateException("인증 서버 점검 중입니다. 잠시 후 다시 로그인해주세요.");
        }

        // HttpOnly 쿠키에 Refresh Token 설정
        long refreshTokenMaxAgeSeconds = refreshTokenDuration.toSeconds();
        cookieUtil.addCookie(response, "refreshToken", refreshToken, refreshTokenMaxAgeSeconds);

        // 프론트 특정 경로로 JWT 담아 리디렉션
        // Refresh Token은 쿠키에 담겨 있으므로 URL에 노출X
        response.sendRedirect(redirectUri + "?token=" + accessToken);
    }
}
