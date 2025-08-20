package com.mxxdone.miniproject.dto.comment;

import com.mxxdone.miniproject.domain.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        return new CommentResponseDto(
                comment.getId(),
                comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent(),
                comment.getAuthor() != null ? comment.getAuthor().getUsername() : null,
                comment.isDeleted(),
                new ArrayList<>(), // 자식 목록은 빈 리스트로 초기화
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}