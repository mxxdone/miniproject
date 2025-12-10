package com.mxxdone.miniproject.dto.post;

import com.mxxdone.miniproject.domain.Post;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 생성 응답 DTO (생성 후 리다이렉트용)")
public record PostSaveResponseDto(
        @Schema(description = "생성된 게시글 ID", example = "10")
        Long postId,

        @Schema(description = "상위 카테고리 슬러그", example = "backend")
        String parentSlug,

        @Schema(description = "하위 카테고리 슬러그", example = "security")
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
