package com.mxxdone.miniproject.dto.comment;

import com.mxxdone.miniproject.domain.Comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record CommentResponseDto(Long id,
                                 String content,
                                 String authorUsername,
                                 boolean isDeleted,
                                 List<CommentResponseDto> children,
                                 LocalDateTime createdAt,
                                 LocalDateTime updatedAt
) {
    public static CommentResponseDto from(Comment comment) {
        return new CommentResponseDto(
                comment.getId(),
                comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent(),
                comment.getAuthor().getUsername(),
                comment.isDeleted(),
                comment.getChildren().stream()
                        .map(CommentResponseDto::from)
                        .collect(Collectors.toList()),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}