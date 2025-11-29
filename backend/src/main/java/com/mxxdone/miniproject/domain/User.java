package com.mxxdone.miniproject.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "USERS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // 시스템 내부용 고유 로그인id

    @Column(nullable = false, unique = true)
    private String nickname; // 화면 표시용

    @Column(nullable = false)
    private String password;

    // Enum 타입을 DB에 저장할 때 문자열로 저장
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false, unique = true)
    private String email;

    private String provider; // 소셜 로그인 제공자(e.g. "google")

    @OneToMany(mappedBy = "author")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "author")
    private List<Comment> comments = new ArrayList<>();

    // 빌더를 써서 각 필드 명까지 입력하도록, 혼동 방지
    @Builder
    public User(String username, String password, Role role, String email, String provider, String nickname) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
        this.provider = provider;
        this.nickname = nickname;
    }
}
