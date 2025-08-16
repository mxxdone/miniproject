package com.mxxdone.miniproject.dto.category;

import com.mxxdone.miniproject.domain.Category;

public record CategoryDto(Long id, String name) {
    public static CategoryDto from(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }
}