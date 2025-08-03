package com.mxxdone.miniproject.service;

import com.mxxdone.miniproject.domain.Role;
import com.mxxdone.miniproject.domain.User;
import com.mxxdone.miniproject.dto.SignUpRequestDto;
import com.mxxdone.miniproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncorder; // SecurityConfig에 등록한 인코더

    public Long signup(SignUpRequestDto requestDto) {
        // 아이디 중복 확인
        if (userRepository.findByUsername(requestDto.username()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncorder.encode(requestDto.password());

        //사용자 생성 (기본 권한은 USER)
        User user = User.builder()
                .username(requestDto.username())
                .password(encodedPassword)
                .role(Role.USER)
                .build();

        // 사용자 저장
        return userRepository.save(user).getId();
    }
}
