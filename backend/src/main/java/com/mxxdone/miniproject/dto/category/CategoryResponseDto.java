package com.mxxdone.miniproject.dto.category;

import com.mxxdone.miniproject.domain.Category;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

// Serializable:
// 스프링의 캐시 기능에게 이 객체를 어떻게 바이트로 변환하고
// 다시 객체로 복원해야 하는지에 대한 명확한 규칙을 알려줌
public record CategoryResponseDto(Long id, String name, String slug, long postCount, List<CategoryResponseDto> children) implements Serializable {
    // category 객체로부터(from) CategoryReponseDto를 만든다
    public static CategoryResponseDto from(Category category) {

        // 자식 DTO 생성
        List<CategoryResponseDto> childrenDtos = category.getChildren().stream()
                .map(CategoryResponseDto::from)
                .collect(Collectors.toList());

        // 자식 카테고리들의 게시글 수 합산
        long childrenPostCount = childrenDtos.stream()
                .mapToLong(CategoryResponseDto::postCount)
                .sum();

        long totalPostCount = category.getPosts().size() + childrenPostCount;

        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getSlug(),
                totalPostCount,
                childrenDtos
        );
    }
}
