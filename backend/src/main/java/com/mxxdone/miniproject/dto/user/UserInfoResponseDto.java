package com.mxxdone.miniproject.dto.user;

import com.mxxdone.miniproject.domain.Role;
import com.mxxdone.miniproject.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 정보 응답 DTO")
public record UserInfoResponseDto(
        @Schema(description = "회원 ID", example = "1")
        Long id,

        @Schema(description = "아이디", example = "user123")
        String username,

        @Schema(description = "닉네임", example = "홍길동")
        String nickname,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "권한", example = "USER")
        Role role,

        @Schema(description = "소셜 로그인 여부" , example = "false")
        boolean isSocialUser
) {
    public static UserInfoResponseDto from(User user) {
        return new UserInfoResponseDto(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getRole(),
                user.getProvider() != null
        );
    }
}
