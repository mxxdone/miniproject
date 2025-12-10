package com.mxxdone.miniproject.controller;

import com.mxxdone.miniproject.dto.user.LoginRequestDto;
import com.mxxdone.miniproject.dto.user.SignUpRequestDto;
import com.mxxdone.miniproject.dto.user.TokenResponseDto;
import com.mxxdone.miniproject.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원 API", description = "회원가입 및 로그인 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 누락, 정규식 불일치)"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 아이디 또는 이메일")
    })
    public ResponseEntity<Long> signup(@RequestBody SignUpRequestDto requestDto) {
        return ResponseEntity.ok(userService.signup(requestDto));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "아이디와 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공 (AccessToken 반환)"),
            @ApiResponse(responseCode = "400", description = "아이디 또는 비밀번호 불일치")
    })
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto requestDto, HttpServletResponse response) {
        return ResponseEntity.ok(userService.login(requestDto, response));
    }
}
