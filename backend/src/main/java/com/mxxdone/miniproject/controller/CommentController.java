package com.mxxdone.miniproject.controller;

import com.mxxdone.miniproject.config.security.PrincipalDetails;
import com.mxxdone.miniproject.dto.comment.CommentResponseDto;
import com.mxxdone.miniproject.dto.comment.CommentSaveRequestDto;
import com.mxxdone.miniproject.dto.comment.CommentUpdateRequestDto;
import com.mxxdone.miniproject.dto.comment.GuestPasswordRequestDto;
import com.mxxdone.miniproject.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
@Tag(name = "댓글 API", description = "댓글 작성, 수정, 삭제 및 조회 기능을 제공합니다.")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "댓글 작성", description = "회원 또는 익명(게스트) 사용자가 댓글을 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값"),
            @ApiResponse(responseCode = "404", description = "게시글 또는 상위 댓글 없음")
    })
    public ResponseEntity<Long> saveComment(
            @Valid @RequestBody CommentSaveRequestDto requestDto,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
        // 비로그인 사용자의 경우 username을 null로 고정
        String username = (principalDetails != null) ? principalDetails.getUsername() : null;
        return ResponseEntity.ok(commentService.save(requestDto, username));
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "게시글별 댓글 조회", description = "특정 게시글에 달린 댓글 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시글이 존재하지 않음")
    })
    public ResponseEntity<List<CommentResponseDto>> getCommentByPost(
            @Parameter(description = "게시글 ID", example = "1", required = true) @PathVariable Long postId
    ) {
        return ResponseEntity.ok(commentService.findByPostId(postId));
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "댓글 수정", description = "댓글 내용을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (내용 누락 등)"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음 (본인 아님)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 댓글")
    })
    public ResponseEntity<Void> updateComment(
            @Parameter(description = "수정할 댓글 ID", example = "10") @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequestDto requestDto,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
        commentService.update(commentId, requestDto, principalDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    // 로그인 사용자 전용 삭제 API
    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제 (회원용)", description = "로그인한 회원이 본인의 댓글을 삭제하거나, 관리자가 댓글을 강제 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 댓글")
    })
    public ResponseEntity<Void> deleteCommentForMember(
            @Parameter(description = "삭제할 댓글 ID", example = "10") @PathVariable Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
        commentService.delete(commentId, principalDetails.getUsername(), null);
        return ResponseEntity.ok().build();
    }

    // 게스트 전용 삭제 API
    @DeleteMapping("/{commentId}/guest")
    @Operation(summary = "댓글 삭제 (게스트용)", description = "비회원이 비밀번호를 입력하여 댓글을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "비밀번호 불일치"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 댓글")
    })
    public ResponseEntity<Void> deleteCommentForGuest(
            @Parameter(description = "삭제할 댓글 ID", example = "10") @PathVariable Long commentId,
            @Valid @RequestBody GuestPasswordRequestDto requestDto) {
        commentService.delete(commentId, null, requestDto.password());
        return ResponseEntity.ok().build();
    }
}
