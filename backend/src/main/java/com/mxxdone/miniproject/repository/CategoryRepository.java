package com.mxxdone.miniproject.repository;

import com.mxxdone.miniproject.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category,Long> {
    // Optional: 결과가 없을 수 있음
    Optional<Category> findByName(String name);
}
