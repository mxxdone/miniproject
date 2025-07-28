package com.mxxdone.miniproject.service;

import com.mxxdone.miniproject.domain.Category;
import com.mxxdone.miniproject.domain.Post;
import com.mxxdone.miniproject.dto.PostResponseDto;
import com.mxxdone.miniproject.dto.PostSaveRequestDto;
import com.mxxdone.miniproject.dto.PostUpdateRequestDto;
import com.mxxdone.miniproject.repository.CategoryRepository;
import com.mxxdone.miniproject.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    // 게시글 저장
    public Long save(PostSaveRequestDto requestDto) {
        Category category = categoryRepository.findById(requestDto.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 없습니다. id= " + requestDto.categoryId()));

        Post post = new Post(requestDto.title(), requestDto.content());
        post.setCategory(category);

        return postRepository.save(post).getId();
        //postRepository-> db에 접근
        //toEntity의 결과 Post 객체를 파라미터로 사용
        //저장후 반환 받은 엔티티 객체에서 id 추출
    }

    //게시글 수정
    public Long update(Long id, PostUpdateRequestDto requestDto) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id= " + id));

        post.update(requestDto.title(), requestDto.content());
        return id;
    }

    //게시글 삭제
    public void delete(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id= " + id));

        postRepository.delete(post);
    }

    //게시글 단건 조회
    //밖으로 나가는 데이터는 DTO로 변환하여 엔티티를 보호
    @Transactional(readOnly = true) //조회 기능은 readOnly = true 옵션으로 성능 최적화
    public PostResponseDto findById(Long id) {
        Post entity = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id= " + id));

        return PostResponseDto.from(entity);
    }

    //게시글 전체 조회
    @Transactional(readOnly = true)
    public Page<PostResponseDto> findAll(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);
        return posts.map(PostResponseDto::from);
    }

}
