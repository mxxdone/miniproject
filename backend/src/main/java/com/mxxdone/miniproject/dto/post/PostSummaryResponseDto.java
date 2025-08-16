package com.mxxdone.miniproject.dto.post;

import com.mxxdone.miniproject.domain.Post;

import java.time.LocalDateTime;

public record PostSummaryResponseDto(
        Long id,
        String title,
        String content,
        String categoryName,
        String authorUsername,
        LocalDateTime createdAt
) {
    public static PostSummaryResponseDto from(Post post) {
        return new PostSummaryResponseDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCategory() != null ? post.getCategory().getName() : null,
                post.getAuthor() != null ? post.getAuthor().getUsername() : null,
                post.getCreatedAt()
        );
    }
}