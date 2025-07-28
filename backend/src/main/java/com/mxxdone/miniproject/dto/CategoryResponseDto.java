package com.mxxdone.miniproject.dto;

import com.mxxdone.miniproject.domain.Category;

public record CategoryResponseDto(Long id, String name) {
    // category 객체로부터(from) CategoryReponseDto를 만든다
    public static CategoryResponseDto from(Category category) {
        return new CategoryResponseDto(category.getId(), category.getName());
    }
}
