package com.mxxdone.miniproject.dto.comment;

import com.mxxdone.miniproject.domain.Comment;

import java.time.LocalDateTime;

public record CommentResponseDto(Long id,
                                 String content,
                                 String authorUsername,
                                 LocalDateTime createdAt,
                                 LocalDateTime updatedAt
) {
    public static CommentResponseDto from(Comment comment) {
        return new CommentResponseDto(
                comment.getId(),
                comment.getContent(),
                comment.getAuthor().getUsername(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}