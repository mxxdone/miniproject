package com.mxxdone.miniproject.service;

import com.mxxdone.miniproject.domain.Post;
import com.mxxdone.miniproject.dto.PostResponseDto;
import com.mxxdone.miniproject.dto.PostSaveRequestDto;
import com.mxxdone.miniproject.dto.PostUpdateRequestDto;
import com.mxxdone.miniproject.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;

    //게시글 저장
    public Long save(PostSaveRequestDto requestDto) {
        return postRepository.save(requestDto.toEntity()).getId();
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
    @Transactional
    public List<PostResponseDto> findAll() {
        return postRepository.findAll().stream()
                //:: -> 가공 없이 파라미터를 그대로 다른 메서드에 전달
                //post를 받아서 from 메서드에 그대로 전달
                //post -> PostResponseDto.from(post)의 축약 버전
                .map(PostResponseDto::from)
                .collect(Collectors.toList());
    }

}
