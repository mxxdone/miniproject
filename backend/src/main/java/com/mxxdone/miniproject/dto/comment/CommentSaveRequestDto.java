package com.mxxdone.miniproject.dto.comment;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mxxdone.miniproject.util.XssSanitizer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentSaveRequestDto(

        @Schema(description = "게시글 ID", example = "1")
        @NotNull(message = "게시글 ID는 필수입니다.")
        Long postId,

        @Schema(description = "댓글 내용", example = "안녕하세요. 테스트 댓글입니다.")
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        @Size(max = 500, message = "댓글은 500자 이내로 작성해주세요.")
        @JsonDeserialize(using = XssSanitizer.class)
        String content,

        @Schema(description = "상위 댓글 ID (대댓글일 경우)", example = "null")
        Long parentId,

        @Schema(description = "게스트 닉네임 (비로그인 시)", example = "게스트회원")
        @Size(max = 10, message = "닉네임은 10자 이내여야 합니다.")
        String guestName,

        @Schema(description = "게스트 비밀번호 (비로그인 시)", example = "1234")
        @Size(max = 50, message = "비밀번호는 50자 이내여야 합니다.")
        String guestPassword
) {
}