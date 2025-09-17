package com.mxxdone.miniproject.dto.comment;

import com.mxxdone.miniproject.domain.Comment;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponseDto(Long id,
                                 String content,
                                 String authorUsername,
                                 boolean isDeleted,
                                 List<CommentResponseDto> children,
                                 LocalDateTime createdAt,
                                 LocalDateTime updatedAt
) {
    public static CommentResponseDto from(Comment comment) {
        String authorName;
        if (comment.getAuthor() != null) {
            authorName = comment.getAuthor().getUsername();
        } else if (comment.getGuestName() != null) {
            authorName = comment.getGuestName();
        } else {
            authorName = "탈퇴한 회원";
        }
        return new CommentResponseDto(
                comment.getId(),
                comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent(),
                authorName,
                comment.isDeleted(),
                comment.getChildren().stream()
                        .map(CommentResponseDto::from)
                        .toList(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}