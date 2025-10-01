package com.mxxdone.miniproject.dto.category;

import com.mxxdone.miniproject.domain.Category;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

// Serializable:
// 스프링의 캐시 기능에게 이 객체를 어떻게 바이트로 변환하고
// 다시 객체로 복원해야 하는지에 대한 명확한 규칙을 알려줌
public record CategoryResponseDto(Long id, String name, List<CategoryResponseDto> children) implements Serializable {
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
