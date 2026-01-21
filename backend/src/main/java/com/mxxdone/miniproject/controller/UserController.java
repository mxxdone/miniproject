package com.mxxdone.miniproject.controller;

import com.mxxdone.miniproject.config.security.PrincipalDetails;
import com.mxxdone.miniproject.dto.user.*;
import com.mxxdone.miniproject.service.UserService;
import com.mxxdone.miniproject.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원 API", description = "회원가입 및 로그인 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final CookieUtil cookieUtil;

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

        LoginResponseDto result = userService.login(requestDto);

        cookieUtil.addCookie(response, "refreshToken", result.refreshToken(), result.maxAge());

        return ResponseEntity.ok(new TokenResponseDto(result.accessToken()));
    }

    @DeleteMapping("/info")
    @Operation(summary = "회원 탈퇴", description = "본인 계정을 탈퇴합니다. 소셜 로그인 사용자는 비밀번호 검증을 건너뜁니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
            @ApiResponse(responseCode = "400", description = "비밀번호 불일치"),
            @ApiResponse(responseCode = "401", description = "로그인 필요")
    })
    public ResponseEntity<Void> withdraw(
            @Valid @RequestBody WithdrawRequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        userService.withdraw(principalDetails.getUsername(), requestDto);

        // 쿠키에서 Refresh Token 삭제
        cookieUtil.deleteCookie(request, response, "refreshToken");

        return ResponseEntity.ok().build();
    }

    @GetMapping("/info")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<UserInfoResponseDto> getMyInfo(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(UserInfoResponseDto.from(principalDetails.getUser()));
    }

    @PatchMapping("/nickname")
    @Operation(summary = "닉네임 수정", description = "현재 로그인한 사용자의 닉네임을 수정합니다.")
    public ResponseEntity<Void> updateNickname(
            @AuthenticationPrincipal PrincipalDetails details,
            @Valid @RequestBody NicknameUpdateRequestDto dto) { // 나눈 DTO 사용
        userService.updateNickname(details.getUser().getId(), dto);
        return ResponseEntity.ok().build();
    }

    // 비밀번호 수정 API (일반 유저 전용)
    @PatchMapping("/password")
    @Operation(summary = "비밀번호 수정", description = "현재 로그인한 사용자의 비밀번호를 수정합니다.")
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal PrincipalDetails details,
            @Valid @RequestBody PasswordUpdateRequestDto dto) { // 나눈 DTO 사용
        userService.updatePassword(details.getUser().getId(), dto);
        return ResponseEntity.ok().build();
    }
}
