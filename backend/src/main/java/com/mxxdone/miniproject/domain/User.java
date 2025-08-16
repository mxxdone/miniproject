package com.mxxdone.miniproject.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "USERS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    // Enum 타입을 DB에 저장할 때 문자열로 저장
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "author")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // 빌더를 써서 각 필드 명까지 입력하도록, 혼동 방지
    @Builder
    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // UserDetails 구현 메서드 추가
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority(this.role.getKey())
        );
    }

    //계정에 대한 정책은 아직 미확립으로 전부 true로 설정

    // 계정 만료 여부(e.g. 1년이상 미접속 휴면 계정)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠금 여부(e.g. 비밀번호 n회 이상 오류)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 비밀번호 만료 여부(e.g. 90일마다 변경 정책 시...)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 활성화 여부(e.g. 관리자가 비활성화)
    @Override
    public boolean isEnabled() {
        return true;
    }
}
