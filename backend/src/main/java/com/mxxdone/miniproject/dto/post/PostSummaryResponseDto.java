package com.mxxdone.miniproject.dto.post;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "게시글 목록 조회용 요약 응답 DTO")
public record PostSummaryResponseDto(
        @Schema(description = "게시글 ID", example = "1")
        Long id,

        @Schema(description = "제목", example = "샘플 게시글 제목")
        String title,

        @Schema(description = "내용 요약 (HTML 제거됨)", example = "샘플 게시글 본문 내용...")
        String content,

        @Schema(description = "카테고리 이름", example = "Backend")
        String categoryName,

        @Schema(description = "작성자 아이디", example = "admin")
        String authorUsername,

        @Schema(description = "작성자 닉네임", example = "관리자")
        String authorNickname,

        @Schema(description = "좋아요 수", example = "15")
        int likeCount,

        @Schema(description = "댓글 수", example = "3")
        Long commentCount,

        @Schema(description = "작성 일시")
        Instant createdAt,

        @Schema(description = "상위 카테고리 슬러그", example = "backend")
        String parentSlug,

        @Schema(description = "하위 카테고리 슬러그", example = "security")
        String childSlug
) {
    // QueryDSL에서 직접 DTO로 조회할 것이므로 @QueryProjection 추가
    // 수정시 clean build 필요
    @QueryProjection
    public PostSummaryResponseDto {}
}