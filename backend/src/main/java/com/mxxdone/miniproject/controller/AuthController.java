package com.mxxdone.miniproject.controller;

import com.mxxdone.miniproject.dto.user.TokenResponseDto;
import com.mxxdone.miniproject.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "인증 API", description = "토큰 재발급 및 로그아웃 기능을 제공합니다.")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "쿠키에 있는 Refresh Token을 이용하여 새로운 Access Token을 발급합니다.")
    public ResponseEntity<TokenResponseDto> refresh(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.refresh(request, response));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "Refresh Token을 삭제하고 로그아웃 처리합니다.")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);

        return ResponseEntity.ok().build();

    }
}
