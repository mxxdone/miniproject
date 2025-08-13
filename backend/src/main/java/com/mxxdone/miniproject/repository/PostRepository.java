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

    /**
     * 선택한 카테고리와 모든 하위 카테고리의 글을 페이지 단위로 조회.
     *
     * WITH RECURSIVE로 시작 카테고리부터 하위 카테고리를 모두 찾음
     * 찾은 카테고리 ID들에 해당하는 게시글만 조회
     */
    @Query(value = "WITH RECURSIVE category_tree AS ( " +
            "    SELECT id FROM category WHERE id = :categoryId " +
            "    UNION ALL " +
            "    SELECT c.id FROM category c JOIN category_tree ct ON c.parent_id = ct.id " +
            ") " +
            "SELECT p.* FROM post p WHERE p.category_id IN (SELECT id FROM category_tree)",
            countQuery = "WITH RECURSIVE category_tree AS ( " +
            "    SELECT id FROM category WHERE id = :categoryId " +
            "    UNION ALL " +
            "    SELECT c.id FROM category c JOIN category_tree ct ON c.parent_id = ct.id " +
            ") " +
            "SELECT count(p.*) FROM post p WHERE p.category_id IN (SELECT id FROM category_tree)",
            nativeQuery = true)
    Page<Post> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);


    // JPQL
    // 제목, 본문 통합 검색
    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Post> searchByTitleOrContent(@Param("keyword") String keyword, Pageable pageable);

    // 제목 검색
    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Post> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    // 본문 검색
    @Query("SELECT p FROM Post p WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Post> searchByContent(@Param("keyword") String keyword, Pageable pageable);

    // 카테고리 내 통합 검색
    @Query("SELECT p FROM Post p WHERE p.category.id = :categoryId AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Post> searchByKeywordAndCategory(@Param("categoryId") Long categoryId, @Param("keyword") String keyword, Pageable pageable);

    // 카테고리 내 제목 검색
    @Query("SELECT p FROM Post p WHERE p.category.id = :categoryId AND LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Post> searchByTitleAndCategory(@Param("categoryId") Long categoryId, @Param("keyword") String keyword, Pageable pageable);

    // 카테고리 내 본문 검색
    @Query("SELECT p FROM Post p WHERE p.category.id = :categoryId AND LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Post> searchByContentAndCategory(@Param("categoryId") Long categoryId, @Param("keyword") String keyword, Pageable pageable);
}
