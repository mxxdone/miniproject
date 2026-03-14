package com.mxxdone.miniproject.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "인기 게시글(조회수 기준) 응답 DTO")
public record PopularPostResponseDto(

        @Schema(description = "게시글 ID", example = "1")
        Long id,

        @Schema(description = "게시글 제목", example = "@transaction이란?")
        String title,

        @Schema(description = "카테고리명", example = "Javascript")
        String categoryName,

        @Schema(description = "상위 카테고리 슬러그", example = "Backend")
        String parentSlug,

        @Schema(description = "하위 카테고리 슬러그", example = "jpa-querydsl")
        String childSlug,

        @Schema(description = "조회수", example = "1024")
        Long viewCount,

        @Schema(description = "좋아요 수", example = "45")
        Long likeCount,

        @Schema(description = "댓글 수", example = "12")
        Long commentCount,

        @Schema(description = "작성일시")
        Instant createdAt
) {}
