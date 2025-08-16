package com.mxxdone.miniproject.service;

import com.mxxdone.miniproject.domain.Category;
import com.mxxdone.miniproject.dto.category.CategoryResponseDto;
import com.mxxdone.miniproject.dto.category.CategorySaveRequestDto;
import com.mxxdone.miniproject.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Long save(CategorySaveRequestDto requestDto) {
        Category category = requestDto.toEntity();

        // 상위 카테고리 id 값을 가지고 상위 카테고리명 저장
        if (requestDto.parentId() != null) {
            Category parent = categoryRepository.findById(requestDto.parentId())
                    .orElseThrow(() -> new IllegalArgumentException("상위 카테고리를 찾을 수 없습니다. id=" + requestDto.parentId()));
            category.setParent(parent);
        }
        return categoryRepository.save(category).getId();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDto> findAll() {
        return categoryRepository.findByParentIsNull().stream()
                .map(CategoryResponseDto::from)
                .collect(Collectors.toList());
    }
}
