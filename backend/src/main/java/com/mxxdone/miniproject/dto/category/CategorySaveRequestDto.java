package com.mxxdone.miniproject.dto.category;

import com.mxxdone.miniproject.domain.Category;

public record CategorySaveRequestDto(String name, Long parentId) {
    public Category toEntity() {
        return new Category(name);
    }
}
