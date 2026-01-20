package com.mxxdone.miniproject.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원 탈퇴 요청 DTO")
public record WithdrawRequestDto(
        @Schema(description = "현재 비밀번호 (소셜 로그인 사용자는 아무 값이나 입력)", example = "password123!")
        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password
) {
}
