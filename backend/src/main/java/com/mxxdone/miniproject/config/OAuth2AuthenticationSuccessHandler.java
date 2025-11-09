package com.mxxdone.miniproject.config;

import com.mxxdone.miniproject.config.security.PrincipalDetails;
import com.mxxdone.miniproject.config.security.jwt.JwtUtil;
import com.mxxdone.miniproject.domain.User;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("====OAuth2.0 Login Success====");
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        User user = principalDetails.getUser();

        // User 객체를 기반으로 JWT 생성
        String token = jwtUtil.createAccessToken(user.getUsername(), user.getRole(), user.getNickname());

        // 프론트 특정 경로로 JWT 담아 리디렉션
        response.sendRedirect(redirectUri + "?token=" + token);
    }
}
