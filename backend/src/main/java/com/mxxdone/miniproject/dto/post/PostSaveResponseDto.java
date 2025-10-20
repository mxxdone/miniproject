package com.mxxdone.miniproject.dto.post;

import com.mxxdone.miniproject.domain.Post;

public record PostSaveResponseDto(
        Long postId,
        String parentSlug,
        String childSlug
) {
    // 정적 팩토리 메소드 (Post 엔티티 -> DTO로 변환)
    public static PostSaveResponseDto from(Post post) {
        String parentSlug = null;
        String childSlug = null;
        if (post.getCategory() != null) {
            childSlug = post.getCategory().getSlug();
            if (post.getCategory().getParent() != null) {
                parentSlug = post.getCategory().getParent().getSlug();
            }
        }
        // Post 객체의 id와 찾아낸 슬러그들로 새로운 DTO 객체를 만들어 반환
        return new PostSaveResponseDto(post.getId(), parentSlug, childSlug);
    }
}
