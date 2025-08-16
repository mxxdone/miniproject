package com.mxxdone.miniproject.dto.category;

import com.mxxdone.miniproject.domain.Category;

import java.util.List;
import java.util.stream.Collectors;

public record CategoryResponseDto(Long id, String name, List<CategoryResponseDto> children) {
    // category 객체로부터(from) CategoryReponseDto를 만든다
    public static CategoryResponseDto from(Category category) {
        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                // 자식 카테고리 목록도 DTO로 변환
                category.getChildren().stream()
                        .map(CategoryResponseDto::from)
                        .collect(Collectors.toList())
        );
    }
}
