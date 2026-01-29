package com.mxxdone.miniproject.repository;

import com.mxxdone.miniproject.domain.Comment;
import com.mxxdone.miniproject.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {
    // Post 객체와 isDeleted 값으로 댓글 수 count
    long countByPostAndIsDeletedFalse(Post post);

    // @Modifying: 조회(SELECT)가 아닌 변경(UPDATE/DELETE) 쿼리임을 명시
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Comment c SET c.isDeleted = true WHERE c.post.id = :postId")
    void softDeleteByPostId(@Param("postId") Long postId);
}