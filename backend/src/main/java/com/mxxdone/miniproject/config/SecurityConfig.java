package com.mxxdone.miniproject.config;

import com.mxxdone.miniproject.config.security.PrincipalOAuth2UserService;
import com.mxxdone.miniproject.config.security.jwt.JwtAuthenticationFilter;
import com.mxxdone.miniproject.config.security.jwt.JwtUtil;
import com.mxxdone.miniproject.service.PrincipalDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 메서드 수준 보안 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final PrincipalDetailService userDetailService;
    private final PrincipalOAuth2UserService principalOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF(Cross-Site Request Forgery) 비활성화
                .csrf(csrf -> csrf.disable())

                // 2. 세션 관리 상태를 STATELESS로 설정 (JWT 사용을 위함)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. FormLogin 비활성화 (JWT 사용을 위해서)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // API 엔드포인트별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        //모든 OPTIONS 요청을 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 토큰 재발급 허용
                        .requestMatchers("/api/v1/auth/refresh").permitAll()
                        // 조회(GET) API들은 누구나 접근 가능
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts/**", "/api/v1/comments/**", "/api/v1/categories/**").permitAll()
                        // 회원가입, 로그인 API는 누구나 접근 가능
                        .requestMatchers("/api/v1/users/signup", "/api/v1/users/login").permitAll()
                        // 게스트 댓글 허용
                        .requestMatchers(HttpMethod.POST, "/api/v1/comments/**").permitAll()
                        // 게스트 댓글 삭제 허용
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/comments/**").permitAll()
                        // OAS(Open API Specification) 관련 접근 허용
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                        //그 외 모든 요청은 인증 사용자만 접근 가능
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(principalOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                );

        http.addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
