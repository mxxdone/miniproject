package com.mxxdone.miniproject.dto.category;

public record CategorySaveRequestDto(
        String name,
        String slug,
        Integer displayOrder,
        Long parentId
) {}
