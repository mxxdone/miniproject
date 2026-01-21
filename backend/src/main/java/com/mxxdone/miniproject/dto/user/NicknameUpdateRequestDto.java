package com.mxxdone.miniproject.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원 비밀번호 수정 요청 DTO")
public record NicknameUpdateRequestDto(
        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(min = 2, max = 10, message = "닉네임은 2~10자여야 합니다.")
        String nickname
) {}
