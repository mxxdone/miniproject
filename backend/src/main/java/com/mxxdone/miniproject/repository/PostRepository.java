package com.mxxdone.miniproject.repository;

import com.mxxdone.miniproject.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

//부모의 설계도
//JpaRepository의 T -> Post, Id -> Long
//모든 제네릭 메서드를 Post 타입에 특화된 버전으로 물려받음
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
}
