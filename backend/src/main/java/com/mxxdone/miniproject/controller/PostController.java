package com.mxxdone.miniproject.controller;

import com.mxxdone.miniproject.config.security.PrincipalDetails;
import com.mxxdone.miniproject.dto.PageDto;
import com.mxxdone.miniproject.dto.post.*;
import com.mxxdone.miniproject.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
@Tag(name = "게시글 API", description = "게시글 작성, 조회, 수정, 삭제 및 좋아요 기능을 제공합니다.")
public class PostController {

    private final PostService postService;

    // 게시글 생성 API
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "게시글 작성", description = "관리자 권한을 가진 사용자만 게시글을 작성할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (관리자 아님)"),
    })
    public ResponseEntity<PostSaveResponseDto> savePost(
            @Valid @RequestBody PostSaveRequestDto requestDto,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails // 현재 로그인한 사용자 정보
    ) {
        return ResponseEntity.ok(postService.save(requestDto, principalDetails.getUser()));
    }

    // 게시글 수정 API
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "게시글 수정", description = "작성자 또는 관리자만 게시글을 수정할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (내용 누락 등)"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글")
    })
    public ResponseEntity<Long> updatePost(
            @Parameter(description = "수정할 게시글 ID") @PathVariable Long id,
            @RequestBody PostUpdateRequestDto requestDto,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(postService.update(id, requestDto, principalDetails.getUser()));
    }

    // 게시글 삭제 API
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "게시글 삭제", description = "작성자 또는 관리자만 게시글을 삭제할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글")
    })
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "삭제할 게시글 ID") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
        postService.delete(id, principalDetails.getUser());
        return ResponseEntity.ok().build();
        //.build() 사용 이유
        //응답 본문(body)이 없는 HTTP응답을 만들기 위해서
        //응답 본문으로 사용할 객체(a.g. PostResponseDto)를 파라미터로 직접 넣어주면
        //ResponseEntity 객체를 반환하지만
        //위의 경우에는 Void로 직접 응답 본문 만들어줘야함.
    }

    // 게시글 단건 조회 API
    @GetMapping("/{id}")
    @Operation(summary = "게시글 단건 조회", description = "게시글의 상세 내용을 조회합니다. 조회수가 1 증가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    public ResponseEntity<PostDetailResponseDto> getPost(
            @Parameter(description = "조회할 게시글 ID") @PathVariable Long id,
            HttpServletRequest request
    ) {
        // 조회수 증가
        postService.incrementViewCount(id, request);
        // 게시글 정보 조회
        PostDetailResponseDto responseDto = postService.findById(id);
        return ResponseEntity.ok(responseDto);
    }

    // 게시글 목록 조회 API
    @GetMapping
    @Operation(summary = "게시글 목록 조회 (검색/페이징)", description = "카테고리, 검색어, 페이징 조건에 맞는 게시글 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<PageDto<PostSummaryResponseDto>> getPosts(
            @Parameter(description = "카테고리 ID (없을 시 전체 조회)") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "검색 유형 (title, content, all)") @RequestParam(required = false, defaultValue = "all") String type,
            @Parameter(description = "검색어") @RequestParam(required = false) String keyword,
            @PageableDefault(size = 6, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.findPosts(categoryId, type, keyword, pageable));
    }

    // 게시글 좋아요 API
    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "게시글 좋아요", description = "게시글에 좋아요를 누르거나 취소합니다. (토글 방식)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좋아요 처리 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글")
    })
    public ResponseEntity<Void> toggleLike(
            @Parameter(description = "게시글 ID") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
        postService.toggleLike(id, principalDetails.getUser());
        return ResponseEntity.ok().build();
    }
}
