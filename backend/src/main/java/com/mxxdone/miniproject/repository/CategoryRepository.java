package com.mxxdone.miniproject.repository;

import com.mxxdone.miniproject.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category,Long> {
    // Optional: 결과가 없을 수 있음
    Optional<Category> findByName(String name);

    // 부모가 없는 최상위 카테고리만 찾는 메서드
    // 미분류 카테고리 (slug = 'uncategorized') 제외
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.slug != 'uncategorized' ORDER BY c.displayOrder ASC")
    List<Category> findAllForDisplay();
}
