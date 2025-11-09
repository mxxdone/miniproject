package com.mxxdone.miniproject.dto.post;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

public record PostSummaryResponseDto(
        Long id,
        String title,
        String content,
        String categoryName,
        String authorUsername,
        String authorNickname,
        int likeCount,
        Long commentCount,
        LocalDateTime createdAt,
        String parentSlug,
        String childSlug
) {
    // QueryDSL에서 직접 DTO로 조회할 것이므로 @QueryProjection 추가
    // 수정시 clean build 필요
    @QueryProjection
    public PostSummaryResponseDto {}
}