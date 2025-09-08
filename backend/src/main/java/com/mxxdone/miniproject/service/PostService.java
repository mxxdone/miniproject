package com.mxxdone.miniproject.service;

import com.mxxdone.miniproject.domain.Category;
import com.mxxdone.miniproject.domain.Post;
import com.mxxdone.miniproject.domain.Role;
import com.mxxdone.miniproject.domain.User;
import com.mxxdone.miniproject.dto.post.PostDetailResponseDto;
import com.mxxdone.miniproject.dto.post.PostSaveRequestDto;
import com.mxxdone.miniproject.dto.post.PostSummaryResponseDto;
import com.mxxdone.miniproject.dto.post.PostUpdateRequestDto;
import com.mxxdone.miniproject.repository.CategoryRepository;
import com.mxxdone.miniproject.repository.PostRepository;
import com.mxxdone.miniproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // 게시글 저장
    public Long save(PostSaveRequestDto requestDto, String username) {
        Category category = categoryRepository.findById(requestDto.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 없습니다. id= " + requestDto.categoryId()));

        if (!category.getChildren().isEmpty()) {
            throw new IllegalArgumentException("하위 카테고리에만 게시글을 작성할 수 있습니다.");
        }

        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 빌더를 이용하여 Post 객체 생성
        Post post = Post.builder()
                .title(requestDto.title())
                .content(requestDto.content())
                .category(category)
                .author(author)
                .build();

        return postRepository.save(post).getId();
        //postRepository-> db에 접근
        //toEntity의 결과 Post 객체를 파라미터로 사용
        //저장후 반환 받은 엔티티 객체에서 id 추출
    }

    //게시글 수정
    public Long update(Long id, PostUpdateRequestDto requestDto, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id= " + id));

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (post.getAuthor() != null && !post.getAuthor().equals(currentUser) && currentUser.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("게시글 수정 권한이 없습니다.");
        }

        post.update(requestDto.title(), requestDto.content());
        return id;
    }

    //게시글 삭제
    public void delete(Long id, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id= " + id));

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (post.getAuthor() != null && !post.getAuthor().equals(currentUser) && currentUser.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("게시글 삭제 권한이 없습니다.");
        }

        postRepository.delete(post);
    }

    //게시글 단건 조회
    //밖으로 나가는 데이터는 DTO로 변환하여 엔티티를 보호
    @Transactional(readOnly = true) //조회 기능은 readOnly = true 옵션으로 성능 최적화
    public PostDetailResponseDto findById(Long id) {
        Post entity = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id= " + id));

        return PostDetailResponseDto.from(entity);
    }

    // 게시글 목록 조회
    @Transactional(readOnly = true)
    public Page<PostSummaryResponseDto> findPosts(Long categoryId, String searchType, String keyword, Pageable pageable) {
        List<Long> categoryIds = null;
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() ->new IllegalArgumentException("해당 카테고리를 찾을 수 없습니다. id= " + categoryId));
            categoryIds = category.getDescendantIdsAndSelf();
        }
        return postRepository.findPostsWithConditions(categoryIds, searchType, keyword, pageable);
    }
}
