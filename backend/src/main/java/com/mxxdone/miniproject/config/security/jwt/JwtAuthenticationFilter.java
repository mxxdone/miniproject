package com.mxxdone.miniproject.config.security.jwt;

import com.mxxdone.miniproject.service.UserDetailServiceImpl;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailServiceImpl userDetailService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = jwtUtil.getTokenFromRequest(request);

        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            // 토큰에서 Claims(페이로드) 추출
            Claims info = jwtUtil.getUserInfoFromToken(token);
            // Claims에서 username 꺼내기
            String username = info.getSubject();

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            // DB에서 username에 해당하는 사용자 정보 가져오기
            UserDetails userDetails = userDetailService.loadUserByUsername(username);
            // UserDetails와 권한 정보를 담아 Authentication 객체 생성
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            context.setAuthentication(authentication);
            // SecurityContextHolder에 context 등록하여 이후 필터/컨트롤러에서 인증 정보 사용 가능
            SecurityContextHolder.setContext(context);
        }
        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}
