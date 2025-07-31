package com.mxxdone.miniproject.repository;

import com.mxxdone.miniproject.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

//부모의 설계도
//JpaRepository의 T -> Post, Id -> Long
//모든 제네릭 메서드를 Post 타입에 특화된 버전으로 물려받음
public interface PostRepository extends JpaRepository<Post, Long> {
    // JpaRepository는 findAll(Pageable)을 기본 제공하지만,
    // 조건 검색(예: 카테고리)과 함께 페이징하려면 직접 메서드를 정의하고 Page<T>로 반환해야 함.
    Page<Post> findByCategoryId(Long categoryId, Pageable pageable);

    // JPQL
    // 제목, 본문 통합 검색
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    Page<Post> searchByTitleOrContent(@Param("keyword") String keyword, Pageable pageable);

    // 제목 검색
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword%")
    Page<Post> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    // 본문 검색
    @Query("SELECT p FROM Post p WHERE p.content LIKE %:keyword%")
    Page<Post> searchByContent(@Param("keyword") String keyword, Pageable pageable);

    // 카테고리 내 통합 검색
    @Query("SELECT p FROM Post p WHERE p.category.id = :categoryId AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
    Page<Post> searchByKeywordAndCategory(@Param("categoryId") Long categoryId, @Param("keyword") String keyword, Pageable pageable);

    // 카테고리 내 제목 검색
    @Query("SELECT p FROM Post p WHERE p.category.id = :categoryId AND p.title LIKE %:keyword%")
    Page<Post> searchByTitleAndCategory(@Param("categoryId") Long categoryId, @Param("keyword") String keyword, Pageable pageable);

    // 카테고리 내 본문 검색
    @Query("SELECT p FROM Post p WHERE p.category.id = :categoryId AND p.content LIKE %:keyword%")
    Page<Post> searchByContentAndCategory(@Param("categoryId") Long categoryId, @Param("keyword") String keyword, Pageable pageable);
}
