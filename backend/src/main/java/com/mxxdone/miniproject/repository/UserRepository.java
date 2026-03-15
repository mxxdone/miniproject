package com.mxxdone.miniproject.repository;

import com.mxxdone.miniproject.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // DB에서 사용자 정보 찾고 비밀번호 비교는 Spring Security에서 처리
    Optional<User> findByUsername(String username);
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);

    // 탈퇴한 회원(is_deleted=true)까지 전부 포함해서 아이디 존재 여부 확인
    @Query(value = "SELECT EXISTS (SELECT 1 FROM users WHERE username = :username)", nativeQuery = true)
    boolean existsByUsernameIgnoreDeleted(@Param("username") String username);
}
