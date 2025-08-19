package com.mxxdone.miniproject.dto.comment;

public record CommentSaveRequestDto(Long postId, String content, Long parentId) {
}