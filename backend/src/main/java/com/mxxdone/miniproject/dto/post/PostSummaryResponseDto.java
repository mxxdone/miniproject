package com.mxxdone.miniproject.dto.post;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

public record PostSummaryResponseDto(
        Long id,
        String title,
        String content,
        String categoryName,
        String authorUsername,
        Long commentCount,
        LocalDateTime createdAt
) {
    // QueryDSL에서 직접 DTO로 조회할 것이므로 @QueryProjection 추가
    @QueryProjection
    public PostSummaryResponseDto {}
}