package com.mxxdone.miniproject.repository;

import com.mxxdone.miniproject.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // DB에서 사용자 정보 찾고 비밀번호 비교는 Spring Security에서 처리
    Optional<User> findByUsername(String username);
    // 닉네임 중복 확인
    boolean existsByNickname(String nickname);
    // 이메일 중복 확인
    boolean existsByEmail(String email);
}
