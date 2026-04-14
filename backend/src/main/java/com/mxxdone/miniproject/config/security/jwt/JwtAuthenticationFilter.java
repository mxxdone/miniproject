package com.mxxdone.miniproject.config.security.jwt;

import com.mxxdone.miniproject.config.security.PrincipalDetails;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        // /api/v1/auth/refresh 또는 logout 경로의 경우, 이 필터를 건너뛰도록
        if ("/api/v1/auth/refresh".equals(requestURI) || "/api/v1/auth/logout".equals(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = jwtUtil.getTokenFromRequest(request);

        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            // 토큰에서 Claims(페이로드) 추출
            Claims info = jwtUtil.getUserInfoFromToken(token);
            // Claims에서 유저명, pk, 권한 꺼내기
            String username = info.getSubject();
            String nickname = info.get("nickname", String.class);
            Long userId = info.get("id", Long.class);
            String roleStr = info.get("auth", String.class);

            // 시큐리티 권한 객체 만들기
            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(roleStr));

            // 프록시 PrincipalDeatils 객체 생성
            // 토큰에서 가져온 값으로 껍데기 생성
            PrincipalDetails principalDetails = new PrincipalDetails(userId, username, nickname, roleStr);

            // Authentication 객체 생성 및 SecurityContext에 저장
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, authorities);
            context.setAuthentication(authentication);
            // SecurityContextHolder에 context 등록하여 이후 필터/컨트롤러에서 인증 정보 사용 가능
            SecurityContextHolder.setContext(context);
        }
        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}
