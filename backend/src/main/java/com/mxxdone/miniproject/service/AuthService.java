package com.mxxdone.miniproject.service;

import com.mxxdone.miniproject.config.security.jwt.JwtUtil;
import com.mxxdone.miniproject.domain.User;
import com.mxxdone.miniproject.dto.user.TokenResponseDto;
import com.mxxdone.miniproject.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급
     *
     * 전체 흐름:
     * 1. 클라이언트가 보낸 refresh token을 추출하여 유효성 검증
     * 2. 토큰에서 사용자 정보를 추출하고 DB에서 사용자 조회
     * 3. 클라이언트의 토큰과 DB에 저장된 토큰을 비교
     * 4. 검증 성공 시 새로운 토큰 발급 및 DB 업데이트
     *
     * @param request HTTP 요청 (헤더에 refresh token이 포함됨)
     * @return 새로 발급된 Access Token과 Refresh Token
     */
    public TokenResponseDto refresh(HttpServletRequest request) {
        String refreshToken = jwtUtil.getTokenFromRequest(request);

        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            log.error("리프레시 토큰 검증 실패 token: {}", refreshToken);
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        Claims info = jwtUtil.getUserInfoFromToken(refreshToken);
        String username = info.getSubject();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!refreshToken.equals(user.getRefreshToken())) {
            log.error("DB의 리프레시 토큰과 일치하지 않습니다. Provided: {}, DB: {}", refreshToken, user.getRefreshToken());
            throw new IllegalArgumentException("저장된 리프레시 토큰과 일치하지 않습니다.");
        }

        String newAccessToken = jwtUtil.createAccessToken(user.getUsername(), user.getRole());
        String newRefreshToken = jwtUtil.createRefreshToken(user.getUsername());
        user.updateRefreshToken(newRefreshToken);

        userRepository.save(user);

        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }
}
