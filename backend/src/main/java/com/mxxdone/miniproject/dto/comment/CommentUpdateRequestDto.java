package com.mxxdone.miniproject.dto.comment;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mxxdone.miniproject.util.XssSanitizer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "댓글 수정 요청 DTO")
public record CommentUpdateRequestDto(
        @Schema(description = "수정할 댓글 내용", example = "내용을 수정합니다.")
        @NotBlank(message = "수정할 내용을 입력해주세요.")
        @Size(max = 500, message = "댓글은 500자 이내로 작성해주세요.")
        @JsonDeserialize(using = XssSanitizer.class)
        String content
) {
}