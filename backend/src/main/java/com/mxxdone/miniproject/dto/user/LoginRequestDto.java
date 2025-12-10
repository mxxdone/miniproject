package com.mxxdone.miniproject.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청 DTO")
public record LoginRequestDto(

        @Schema(description = "아이디", example = "user1234")
        @NotBlank(message = "아이디를 입력해주세요.")
        String username,

        @Schema(description = "비밀번호", example = "pass1234!@")
        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password
) {
}
