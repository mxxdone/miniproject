package com.mxxdone.miniproject.repository;

import com.mxxdone.miniproject.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // join fetch로 일괄 조회 후 distinct로 중복 엔티티 제거
    @Query("""
        select distinct c
        from Comment c
        left join fetch c.author
        left join fetch c.children ch
        left join fetch ch.author
        where c.post.id = :postId and c.parent is null
        order by c.createdAt asc
    """)
    List<Comment> findRootsWithChildren(@Param("postId") Long postId);}