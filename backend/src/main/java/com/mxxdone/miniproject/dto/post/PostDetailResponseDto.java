package com.mxxdone.miniproject.dto.post;

import com.mxxdone.miniproject.dto.category.CategoryDto;

import java.time.LocalDateTime;
import java.util.List;

//게시글 정보 불러올 때 사용하는 DTO
//불변 데이터를 다룰 때 record 사용
//계층 간 데이터를 안전하게 전달하는 것이 주된 목적
//데이터 변경 가능성을 원천적으로 차단 -> 시스템 안정성 향상
public record PostDetailResponseDto(
        Long id,
        String title,
        String content,
        List<CategoryDto> categoryPath,
        Long categoryId,
        String authorUsername,
        String authorNickname,
        int viewCount,
        int likeCount,
        boolean isLiked,
        long commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
