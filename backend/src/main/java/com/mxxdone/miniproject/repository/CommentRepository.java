package com.mxxdone.miniproject.repository;

import com.mxxdone.miniproject.domain.Comment;
import com.mxxdone.miniproject.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {
    // Post 객체와 isDeleted 값으로 댓글 수 count
    long countByPostAndIsDeletedFalse(Post post);
}