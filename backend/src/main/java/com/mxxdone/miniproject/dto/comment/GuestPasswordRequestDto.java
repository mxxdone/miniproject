package com.mxxdone.miniproject.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "게스트 댓글 삭제/수정 권한 확인 DTO")
public record GuestPasswordRequestDto(
        @Schema(description = "비밀번호", example = "1234")
        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(max = 50, message = "비밀번호는 50자 이내여야 합니다.")
        String password
) {
}
