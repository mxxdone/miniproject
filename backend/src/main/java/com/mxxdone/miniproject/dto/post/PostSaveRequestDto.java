package com.mxxdone.miniproject.dto.post;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mxxdone.miniproject.util.TitleSanitizer;
import com.mxxdone.miniproject.util.XssSanitizer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

//게시글 생성 요청 DTO
public record PostSaveRequestDto(

        @Schema(description = "게시글 제목 (HTML 태그 제거됨)", example = "샘플 게시물 제목")
        @NotBlank(message = "제목은 필수 입력 값입니다.") // 빈 값 방지
        @Size(max = 200, message = "제목은 200자 이내여야 합니다.") // 길이 제한
        @JsonDeserialize(using = TitleSanitizer.class)
        String title,

        @Schema(description = "게시글 본문 (XSS 필터링 적용)", example = "<p>샘플 게시물 본문...</p>")
        @NotBlank(message = "내용은 필수 입력 값입니다.") // 빈 값 방지
        @JsonDeserialize(using = XssSanitizer.class)
        String content,

        @Schema(description = "카테고리 ID", example = "1")
        Long categoryId
) {
}
