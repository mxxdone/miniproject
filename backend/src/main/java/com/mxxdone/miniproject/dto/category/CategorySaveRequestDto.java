package com.mxxdone.miniproject.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "카테고리 생성 요청 DTO")
public record CategorySaveRequestDto(

        @Schema(description = "카테고리 이름", example = "Spring Boot")
        @NotBlank(message = "카테고리 이름을 입력해주세요.")
        String name,

        @Schema(description = "URL 슬러그 (영문 소문자, 숫자, 하이픈만 허용)", example = "spring-boot")
        @NotBlank(message = "슬러그를 입력해주세요.")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "슬러그는 영문 소문자, 숫자, 하이픈(-)만 사용할 수 있습니다.")
        String slug,

        @Schema(description = "표시 순서", example = "1")
        @NotNull(message = "순서를 입력해주세요.")
        @Min(value = 0, message = "순서는 0 이상이어야 합니다.")
        Integer displayOrder,

        @Schema(description = "상위 카테고리 ID (최상위면 null)", example = "null")
        Long parentId
) {}
