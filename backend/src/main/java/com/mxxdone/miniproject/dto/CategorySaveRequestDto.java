package com.mxxdone.miniproject.dto;

import com.mxxdone.miniproject.domain.Category;

public record CategorySaveRequestDto(String name) {
    public Category toEntity() {
        return new Category(name);
    }
}
