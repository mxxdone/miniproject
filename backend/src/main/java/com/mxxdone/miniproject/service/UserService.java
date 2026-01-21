package com.mxxdone.miniproject.service;

import com.mxxdone.miniproject.config.security.jwt.JwtUtil;
import com.mxxdone.miniproject.domain.Role;
import com.mxxdone.miniproject.domain.User;
import com.mxxdone.miniproject.dto.user.*;
import com.mxxdone.miniproject.exception.DuplicateException;
import com.mxxdone.miniproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // SecurityConfig에 등록한 인코더
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public Long signup(SignUpRequestDto requestDto) {
        // 아이디 중복 확인
        if (userRepository.findByUsername(requestDto.username()).isPresent()) {
            throw new DuplicateException("이미 사용 중인 아이디입니다.");
        }

        if (userRepository.existsByEmail(requestDto.email())) {
            throw new DuplicateException("이미 사용 중인 이메일입니다.");
        }

        if (userRepository.existsByNickname(requestDto.nickname())) {
            throw new DuplicateException("이미 사용 중인 닉네임입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.password());

        //사용자 생성 (기본 권한은 USER)
        User user = User.builder()
                .username(requestDto.username())
                .password(encodedPassword)
                .nickname(requestDto.nickname())
                .email(requestDto.email())
                .role(Role.USER)
                .build();

        // 사용자 저장
        return userRepository.save(user).getId();
    }

    public LoginResponseDto login(LoginRequestDto requestDto) {
        User user = userRepository.findByUsername(requestDto.username())
                .orElseThrow(() -> new NoSuchElementException("등록된 사용자가 없습니다."));

        // matches: 사용자 입력값과 DB 저장 해시값을 비교해줌
        if (!passwordEncoder.matches(requestDto.password(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtUtil.createAccessToken(user.getUsername(), user.getRole(), user.getNickname());
        String refreshToken = jwtUtil.createRefreshToken(user.getUsername());

        // Redis에 저장
        Duration refreshTokenDuration = Duration.ofDays(jwtUtil.getRefreshTokenExpirationDays()); // 만료 기간 Duration 객체로 생성
        refreshTokenService.saveRefreshToken(user.getUsername(), refreshToken, refreshTokenDuration);

        return new LoginResponseDto(accessToken, refreshToken, refreshTokenDuration.toSeconds());
    }

    public void withdraw(String username, WithdrawRequestDto requestDto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        // 소셜 로그인 사용자가 아닌 경우(일반회원) -> 비밀번호 검증
        if (user.getProvider() == null) {
            if (requestDto.password() == null || requestDto.password().isBlank()) {
                throw new IllegalArgumentException("비밀번호를 입력해주세요.");
            }
            if (!passwordEncoder.matches(requestDto.password(), user.getPassword())) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
        }
        // Redis에서 Refresh Token 삭제
        try {
            refreshTokenService.deleteRefreshToken(username);
        } catch (Exception e) {
            log.error("회원 탈퇴 중 Redis 토큰 삭제 실패 (username: {}", username, e);
        }

        userRepository.delete(user); // soft delete (@SQLDelete)
        log.info("회원 탈퇴 완료: username={}", username);
    }

    // 닉네임 수정: 소셜/일반 공통
    public void updateNickname(Long userId, NicknameUpdateRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        // 현재와 동일하면 스킵
        if (user.getNickname().equals(requestDto.nickname())) {
            return;
        }

        // 중복 확인
        if (userRepository.existsByNickname(requestDto.nickname())) {
            throw new DuplicateException("이미 사용 중인 닉네임입니다.");
        }

        user.updateNickname(requestDto.nickname());
    }

    // 비밀번호 수정: 일반 유저 전용
    public void updatePassword(Long userId, PasswordUpdateRequestDto requestDto) {
        User user = userRepository.findById(userId).orElseThrow();

        // 현재 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(requestDto.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호가 기존 비밀번호와 동일한지 확인
        if (passwordEncoder.matches(requestDto.newPassword(), user.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호는 기존 비밀번호와 다르게 설정해야 합니다.");
        }

        user.updatePassword(passwordEncoder.encode(requestDto.newPassword()));
    }
}
