package com.mxxdone.miniproject.dto.category;

import com.mxxdone.miniproject.domain.Category;

public record CategorySaveRequestDto(String name, String slug, Integer displayOrder, Long parentId) {
    public Category toEntity() {
        return new Category(name, slug, displayOrder);
    }
}
