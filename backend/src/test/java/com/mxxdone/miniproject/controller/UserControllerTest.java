package com.mxxdone.miniproject.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mxxdone.miniproject.config.OAuth2AuthenticationSuccessHandler;
import com.mxxdone.miniproject.config.SecurityConfig;
import com.mxxdone.miniproject.config.security.PrincipalOAuth2UserService;
import com.mxxdone.miniproject.config.security.jwt.JwtUtil;
import com.mxxdone.miniproject.dto.user.LoginRequestDto;
import com.mxxdone.miniproject.dto.user.SignUpRequestDto;
import com.mxxdone.miniproject.dto.user.TokenResponseDto;
import com.mxxdone.miniproject.service.PrincipalDetailService;
import com.mxxdone.miniproject.service.RefreshTokenService;
import com.mxxdone.miniproject.service.UserService;
import com.mxxdone.miniproject.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class) // UserController만 테스트하도록 지정
@Import(SecurityConfig.class)     // SecurityConfig를 함께 로드
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private UserService userService;

    // @Import(SecurityConfig.class)
    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private PrincipalDetailService principalDetailService;
    @MockitoBean
    private PrincipalOAuth2UserService principalOAuth2UserService;
    @MockitoBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    @MockitoBean
    private RefreshTokenService refreshTokenService;
    @MockitoBean
    private CookieUtil cookieUtil;

    @Test
    @DisplayName("회원가입 API가 정상적으로 호출되면 200 OK 반환")
    void signup_api_success() throws Exception {
        // given: UserService의 signup 메서드는 어떤 값이 들어오든 1L을 반환하도록 설정
        SignUpRequestDto requestDto = new SignUpRequestDto("testuser", "password123!", "닉네임", "test@example.com");
        given(userService.signup(any(SignUpRequestDto.class))).willReturn(1L);

        // when & then
        mockMvc.perform(post("/api/v1/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    @DisplayName("로그인 API가 정상적으로 호출되면 200 OK / 토큰 DTO를 JSON 형태로 반환")
    void login_api_success() throws Exception {
        // given: UserService의 login 메서드는 TokenResponseDto를 반환하도록 설정
        LoginRequestDto requestDto = new LoginRequestDto("testuser", "password123!");

        TokenResponseDto tokenDto = new TokenResponseDto(
                "mock-access-token"
        );

        given(userService.login(any(LoginRequestDto.class), any(HttpServletResponse.class))).willReturn(tokenDto);

        // when & then
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-access-token"));
    }
}