package com.mxxdone.miniproject.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "최근 작성된 댓글 DTO")
public record RecentCommentResponseDto(

        @Schema(description = "댓글 ID", example = "50")
        Long id,

        @Schema(description = "댓글 내용", example = "좋은 정보 감사합니다!")
        String content,

        @Schema(description = "작성자 닉네임", example = "홍길동")
        String authorNickname,

        @Schema(description = "게시글 ID", example = "1")
        Long postId,

        @Schema(description = "게시글 제목", example = "@transaction이란?")
        String postTitle,

        @Schema(description = "상위 카테고리 슬러그", example = "Backend")
        String parentSlug,

        @Schema(description = "하위 카테고리 슬러그", example = "jpa-querydsl")
        String childSlug,

        @Schema(description = "작성일시")
        Instant createdAt
) {}