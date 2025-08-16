package com.mxxdone.miniproject.controller;

import com.mxxdone.miniproject.dto.user.LoginRequestDto;
import com.mxxdone.miniproject.dto.user.SignUpRequestDto;
import com.mxxdone.miniproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@RequestBody SignUpRequestDto requestDto) {
        return ResponseEntity.ok(userService.signup(requestDto));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto requestDto) {
        String token = userService.login(requestDto);
        // 토큰을 클라이언트에게 전달
        return ResponseEntity.ok(token);
    }
}
