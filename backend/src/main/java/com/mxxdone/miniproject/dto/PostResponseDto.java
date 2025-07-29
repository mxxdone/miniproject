package com.mxxdone.miniproject.dto;

import com.mxxdone.miniproject.domain.Post;

import java.time.LocalDateTime;

//게시글 정보 불러올 때 사용하는 DTO
//불변 데이터를 다룰 때 record 사용
//계층 간 데이터를 안전하게 전달하는 것이 주된 목적
//데이터 변경 가능성을 원천적으로 차단 -> 시스템 안정성 향상
public record PostResponseDto(
        Long id,
        String title,
        String content,
        String categoryName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    //Post 엔티티 객체를 받아서 Dto로 변환
    public static PostResponseDto from(Post post) {
        return new PostResponseDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCategory() != null ? post.getCategory().getName() : null,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
