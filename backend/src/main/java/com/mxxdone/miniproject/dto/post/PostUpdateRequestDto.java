package com.mxxdone.miniproject.dto.post;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mxxdone.miniproject.util.TitleSanitizer;
import com.mxxdone.miniproject.util.XssSanitizer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostUpdateRequestDto(
        @Schema(description = "수정할 제목", example = "샘플 게시물 제목 (수정)")
        @NotBlank(message = "제목은 필수 입력 값입니다.")
        @Size(max = 200, message = "제목은 200자 이내여야 합니다.")
        @JsonDeserialize(using = TitleSanitizer.class)
        String title,

        @Schema(description = "수정할 본문", example = "<p>내용을 수정했습니다.</p>")
        @NotBlank(message = "내용은 필수 입력 값입니다.")
        @Size(max = 10000, message = "내용은 10,000자를 넘을 수 없습니다.")
        @JsonDeserialize(using = XssSanitizer.class)
        String content,

        @Schema(description = "변경할 카테고리 ID (null이면 변경 안 함)", example = "2")
        Long categoryId
) {
}
