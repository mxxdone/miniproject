package com.mxxdone.miniproject.repository;

import com.mxxdone.miniproject.domain.Comment;

import java.util.List;

public interface CommentRepositoryCustom {
    List<Comment> findVisibleCommentsByPostId(Long postId);
}
