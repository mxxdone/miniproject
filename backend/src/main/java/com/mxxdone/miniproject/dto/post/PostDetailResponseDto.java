package com.mxxdone.miniproject.dto.post;

import com.mxxdone.miniproject.dto.category.CategoryDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

//게시글 정보 불러올 때 사용하는 DTO
//불변 데이터를 다룰 때 record 사용
//계층 간 데이터를 안전하게 전달하는 것이 주된 목적
//데이터 변경 가능성을 원천적으로 차단 -> 시스템 안정성 향상
public record PostDetailResponseDto(
        @Schema(description = "게시글 ID") Long id,
        @Schema(description = "제목") String title,
        @Schema(description = "본문 내용") String content,
        @Schema(description = "카테고리 경로 (상위->하위)") List<CategoryDto> categoryPath,
        @Schema(description = "현재 카테고리 ID") Long categoryId,
        @Schema(description = "작성자 아이디") String authorUsername,
        @Schema(description = "작성자 닉네임") String authorNickname,
        @Schema(description = "조회수") int viewCount,
        @Schema(description = "좋아요 수") int likeCount,
        @Schema(description = "현재 사용자의 좋아요 여부") boolean isLiked,
        @Schema(description = "댓글 수") long commentCount,
        @Schema(description = "작성일시") Instant createdAt,
        @Schema(description = "수정일시") Instant updatedAt
) {}
