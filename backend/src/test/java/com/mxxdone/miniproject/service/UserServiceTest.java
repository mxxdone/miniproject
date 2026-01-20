package com.mxxdone.miniproject.service;

import com.mxxdone.miniproject.domain.User;
import com.mxxdone.miniproject.dto.user.LoginRequestDto;
import com.mxxdone.miniproject.dto.user.LoginResponseDto;
import com.mxxdone.miniproject.dto.user.SignUpRequestDto;
import com.mxxdone.miniproject.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class UserServiceTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private S3Uploader s3Uploader;
    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("정상적인 정보로 회원가입에 성공")
    void signup_success() {
        // given
        SignUpRequestDto requestDto = new SignUpRequestDto(
                "testuser001",
                "password1234!",
                "테스트유저1",
                "test@example.com"
        );

        // when
        Long userId = userService.signup(requestDto);

        // then
        assertThat(userId).isNotNull(); // 회원가입 성공하면 userId 반환됨 (notNull)

        // 들어간 정보는 문제 없는지
        User savedUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        assertThat(savedUser.getUsername()).isEqualTo("testuser001");
        // 비밀번호는 암호화 되어서 같으면 안됨
        assertThat(savedUser.getPassword()).isNotEqualTo("password1234!");
        assertThat(passwordEncoder.matches("password1234!", savedUser.getPassword())).isTrue();
        assertThat(savedUser.getNickname()).isEqualTo("테스트유저1");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("중복 아이디로 회원가입시 예외 발생")
    void signup_fail_duplicate_username() {
        // given
        SignUpRequestDto firstRequestDto = new SignUpRequestDto(
                "testuser001",
                "password1234!",
                "테스트유저1",
                "test@example.com"
        );
        userService.signup(firstRequestDto);

        // when
        SignUpRequestDto secondReuqestDto = new SignUpRequestDto(
                "testuser001",
                "anotherpw123!",
                "테스트유저2",
                "test2@example.com"
        );

        // then
        assertThatThrownBy(() -> userService.signup(secondReuqestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 아이디입니다.");
    }

    @Test
    @DisplayName("중복된 이메일로 회원가입시 예외 발생")
    void sighup_fail_duplicate_email() {
        // given
        SignUpRequestDto firstRequestDto = new SignUpRequestDto(
                "testuser001",
                "password1234!",
                "테스트유저1",
                "test@example.com"
        );
        userService.signup(firstRequestDto);
        // when
        SignUpRequestDto secondRequestDto = new SignUpRequestDto(
                "testuser002",
                "anotherpw123!",
                "테스트유저2",
                "test@example.com"
        );
        // then
        assertThatThrownBy(() -> userService.signup(secondRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");
    }
    
    @Test
    @DisplayName("중복된 닉네임으로 회원가입시 예외 발생")
    void sighup_fail_duplicate_nickname() {
        // given
        SignUpRequestDto firstRequestDto = new SignUpRequestDto(
                "testuser001",
                "password1234!",
                "테스트유저1",
                "test@example.com"
        );
        userService.signup(firstRequestDto);
        // when
        SignUpRequestDto secondRequestDto = new SignUpRequestDto(
                "testuser002",
                "anotherpw123!",
                "테스트유저1",
                "test22@example.com"
        );
        // then
        assertThatThrownBy(() -> userService.signup(secondRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 닉네임입니다.");
    }

    @Test
    @DisplayName("정상 정보로 로그인 성공시 JWT 토큰 반환")
    void login_success() {
        // given
        SignUpRequestDto signUpRequestDto = new SignUpRequestDto(
                "testuser001",
                "password1234!",
                "테스트유저1",
                "test@example.com"
        );
        userService.signup(signUpRequestDto);

        LoginRequestDto loginRequestDto = new LoginRequestDto(
                "testuser001",
                "password1234!"
        );

        // when
        LoginResponseDto result = userService.login(loginRequestDto);

        // 반환된 데이터 값 검증
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isNotNull().isNotEmpty();
        assertThat(result.refreshToken()).isNotNull().isNotEmpty();
        assertThat(result.maxAge()).isGreaterThan(0);

        // Redis에 Refresh Token이 정상적으로 저장되었는지 검증
        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> refreshTokenCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);

        verify(refreshTokenService).saveRefreshToken(usernameCaptor.capture(), refreshTokenCaptor.capture(), durationCaptor.capture());

        assertThat(usernameCaptor.getValue()).isEqualTo("testuser001");
        assertThat(refreshTokenCaptor.getValue()).isEqualTo(result.refreshToken());
        assertThat(durationCaptor.getValue()).isNotNull(); // Duration 값 검증
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 예외 발생")
    void login_fail_wrong_password() {
        // given
        SignUpRequestDto signUpRequestDto = new SignUpRequestDto(
                "testuser001",
                "password1234!",
                "테스트유저1",
                "test@example.com"
        );
        userService.signup(signUpRequestDto);
        LoginRequestDto loginRequestDto = new LoginRequestDto(
                "testuser001",
                "wrongpw1234!"
        );

        // then
        assertThatThrownBy(() -> userService.login(loginRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }
}
