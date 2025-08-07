package com.mxxdone.miniproject.controller;

import com.mxxdone.miniproject.dto.PostResponseDto;
import com.mxxdone.miniproject.dto.PostSaveRequestDto;
import com.mxxdone.miniproject.dto.PostUpdateRequestDto;
import com.mxxdone.miniproject.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    //게시글 생성 API
    @PostMapping
    public ResponseEntity<Long> savePost(
            @RequestBody PostSaveRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails // 현재 로그인한 사용자 정보
    ) {
        String username = userDetails.getUsername();
        return ResponseEntity.ok(postService.save(requestDto, username));
    }

    //게시글 수정 API
    @PutMapping("/{id}")
    public ResponseEntity<Long> updatePost(@PathVariable Long id,
                                           @RequestBody PostUpdateRequestDto requestDto,
                                           @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(postService.update(id, requestDto, userDetails.getUsername()));
    }

    //게시글 삭제 API
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails userDetails
    ) {
        postService.delete(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
        //.build() 사용 이유
        //응답 본문(body)이 없는 HTTP응답을 만들기 위해서
        //응답 본문으로 사용할 객체(a.g. PostResponseDto)를 파라미터로 직접 넣어주면
        //ResponseEntity 객체를 반환하지만
        //위의 경우에는 Void로 직접 응답 본문 만들어줘야함.
    }

    //게시글 단건 조회 API
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.findById(id));
    }

    //게시글 전체 조회 API
    @GetMapping
//    public ResponseEntity<List<PostResponseDto>> getAllPosts() {
    public ResponseEntity<Page<PostResponseDto>> getAllPosts(
            @PageableDefault(size = 6, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.findAll(pageable));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<PostResponseDto>> getPostsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 6, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.findByCategoryId(categoryId, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PostResponseDto>> searchPosts(
            @RequestParam(required = false, defaultValue = "all") String type,
            @RequestParam String keyword,
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 6, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.searchByKeyword(type, keyword, categoryId, pageable));
    }
}
