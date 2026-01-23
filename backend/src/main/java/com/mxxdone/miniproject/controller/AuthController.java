package com.mxxdone.miniproject.controller;

import com.mxxdone.miniproject.dto.user.LoginRequestDto;
import com.mxxdone.miniproject.dto.user.LoginResponseDto;
import com.mxxdone.miniproject.dto.user.TokenResponseDto;
import com.mxxdone.miniproject.service.AuthService;
import com.mxxdone.miniproject.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "인증 API", description = "토큰 재발급 및 로그아웃 기능을 제공합니다.")
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "쿠키에 있는 Refresh Token을 이용하여 새로운 Access Token을 발급합니다.")
    public ResponseEntity<TokenResponseDto> refresh(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.refresh(request, response));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "아이디와 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공 (AccessToken 반환)"),
            @ApiResponse(responseCode = "400", description = "아이디 또는 비밀번호 불일치")
    })
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto requestDto, HttpServletResponse response) {

        LoginResponseDto result = authService.login(requestDto);
        cookieUtil.addCookie(response, "refreshToken", result.refreshToken(), result.maxAge());

        return ResponseEntity.ok(new TokenResponseDto(result.accessToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "Refresh Token을 삭제하고 로그아웃 처리합니다.")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);

        return ResponseEntity.ok().build();
    }
}
