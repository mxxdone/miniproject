package com.mxxdone.miniproject.dto.post;

import com.mxxdone.miniproject.domain.Category;
import com.mxxdone.miniproject.domain.Post;
import com.mxxdone.miniproject.dto.category.CategoryDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//게시글 정보 불러올 때 사용하는 DTO
//불변 데이터를 다룰 때 record 사용
//계층 간 데이터를 안전하게 전달하는 것이 주된 목적
//데이터 변경 가능성을 원천적으로 차단 -> 시스템 안정성 향상
public record PostDetailResponseDto(
        Long id,
        String title,
        String content,
        List<CategoryDto> categoryPath,
        Long categoryId,
        String authorUsername,
        String authorNickname,
        int viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    //Post 엔티티 객체를 받아서 Dto로 변환
    public static PostDetailResponseDto from(Post post) {
        // 카테고리 경로 계산
        List<CategoryDto> categoryPath = new ArrayList<>();
        if (post.getCategory() != null) {
            Category currentCategory = post.getCategory();
            while (currentCategory != null) {
                categoryPath.add(CategoryDto.from(currentCategory));
                currentCategory = currentCategory.getParent();
            }
            Collections.reverse(categoryPath); // 상위 -> 하위 카테고리 순서로 변경
        }
        return new PostDetailResponseDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                categoryPath,
                // 연관관계인 필드만 null 체크
                post.getCategory() != null ? post.getCategory().getId() : null,
                post.getAuthor() != null ? post.getAuthor().getUsername() : null,
                post.getAuthor() != null ? post.getAuthor().getNickname() : null,
                post.getViewCount(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
