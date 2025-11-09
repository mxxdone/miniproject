package com.mxxdone.miniproject.repository;

import com.mxxdone.miniproject.domain.Post;
import com.mxxdone.miniproject.domain.PostLike;
import com.mxxdone.miniproject.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByUserAndPost(User user, Post post);
    boolean existsByUserAndPost(User user, Post post);
}