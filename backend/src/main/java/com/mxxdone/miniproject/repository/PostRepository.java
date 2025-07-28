package com.mxxdone.miniproject.repository;

import com.mxxdone.miniproject.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

//부모의 설계도
//JpaRepository의 T -> Post, Id -> Long
//모든 제네릭 메서드를 Post 타입에 특화된 버전으로 물려받음
public interface PostRepository extends JpaRepository<Post, Long> {
    // JpaRepository는 findAll(Pageable)을 기본 제공하지만,
    // 조건 검색(예: 카테고리)과 함께 페이징하려면 직접 메서드를 정의하고 Page<T>로 반환해야 함.
    Page<Post> findByCategoryId(Long categoryId, Pageable pageable);
}
