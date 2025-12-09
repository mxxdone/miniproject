package com.mxxdone.miniproject.service;

import com.mxxdone.miniproject.config.security.jwt.JwtUtil;
import com.mxxdone.miniproject.domain.Role;
import com.mxxdone.miniproject.domain.User;
import com.mxxdone.miniproject.dto.user.LoginRequestDto;
import com.mxxdone.miniproject.dto.user.SignUpRequestDto;
import com.mxxdone.miniproject.dto.user.TokenResponseDto;
import com.mxxdone.miniproject.repository.UserRepository;
import com.mxxdone.miniproject.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // SecurityConfig에 등록한 인코더
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtil cookieUtil;

    public Long signup(SignUpRequestDto requestDto) {
        // 아이디 중복 확인
        if (userRepository.findByUsername(requestDto.username()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        if (userRepository.existsByEmail(requestDto.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if (userRepository.existsByNickname(requestDto.nickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
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

    public TokenResponseDto login(LoginRequestDto requestDto, HttpServletResponse response) {
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

        long refreshTokenMaxAgeSeconds = refreshTokenDuration.toSeconds();
        cookieUtil.addCookie(response, "refreshToken", refreshToken, refreshTokenMaxAgeSeconds); // response 객체 전달

        return new TokenResponseDto(accessToken);
    }
}
