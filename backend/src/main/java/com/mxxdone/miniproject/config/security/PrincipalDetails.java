package com.mxxdone.miniproject.config.security;

import com.mxxdone.miniproject.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

// User 엔티티를 스프링 시큐리티에 맞는 UserDetails와 OAuth2User 형태로 포장
// 모든 메서드는 User 객체에 요청
@Getter
public class PrincipalDetails implements UserDetails, OAuth2User {

    private User user;
    private Map<String, Object> attributes;

    private final Long id;
    private final String username;
    private final String role;

    // 일반 로그인 시 사용하는 생성자
    public PrincipalDetails(User user) {
        this.user = user;
        this.id = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole().getKey();
    }

    // OAuth 로그인 시 사용하는 생성자
    public PrincipalDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
        this.id = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole().getKey();
    }

    // JWT 필터에서 DB 조회 없이 토큰 정보만으로 객체 만들 때 쓰는 생성자
    public PrincipalDetails(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    // UserDetails 구현
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // User 엔티티의 Role 정보를 가져와서 시큐리티 타입으로 변환
        return Collections.singletonList(
                new SimpleGrantedAuthority(this.role)
        );
    }

    @Override
    public String getPassword() {
        // 토큰 인증 시에는 비밀번호가 필요 없으므로 null 방어
        return user != null ? user.getPassword() : null;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // --- OAuth2User 구현 ---
    @Override
    public Map<String, Object> getAttributes() {
        return attributes; // 생성자에서 받은 attributes를 반환
    }

    @Override
    public String getName() {
        return attributes != null ? (String) attributes.get("sub") : null; // 고유 식별자를 반환
    }
}
