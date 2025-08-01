package com.mxxdone.miniproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

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
                        // 조회(GET) API들은 누구나 접근 가능
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts/**", "api/v1/categories/**").permitAll()
                        // 회원가입, 로그인 API는 누구나 접근 가능 (추후 추가)
                        //.requestMatchers("/api/v1/users/signup", "/api/v1/users/login").permitAll()
                        //그 외 모든 요청은 인증 사용자만 접근 가능
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
