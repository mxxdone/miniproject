package com.mxxdone.miniproject.controller;

import com.mxxdone.miniproject.config.security.PrincipalDetails;
import com.mxxdone.miniproject.dto.comment.CommentResponseDto;
import com.mxxdone.miniproject.dto.comment.CommentSaveRequestDto;
import com.mxxdone.miniproject.dto.comment.CommentUpdateRequestDto;
import com.mxxdone.miniproject.dto.comment.GuestPasswordRequestDto;
import com.mxxdone.miniproject.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<Long> saveComment(@RequestBody CommentSaveRequestDto requestDto,
                                            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        // 비로그인 사용자의 경우 username을 null로 고정
        String username = (principalDetails != null) ? principalDetails.getUsername() : null;
        return ResponseEntity.ok(commentService.save(requestDto, username));
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponseDto>> getCommentByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.findByPostId(postId));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        commentService.update(commentId, requestDto, principalDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    // 로그인 사용자 전용 삭제 API
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteCommentForMember(@PathVariable Long commentId,
                                                       @AuthenticationPrincipal PrincipalDetails principalDetails) {
        commentService.delete(commentId, principalDetails.getUsername(), null);
        return ResponseEntity.ok().build();
    }

    // 게스트 전용 삭제 API
    @DeleteMapping("/{commentId}/guest")
    public ResponseEntity<Void> deleteCommentForGuest(@PathVariable Long commentId,
                                                      @RequestBody GuestPasswordRequestDto requestDto) {
        commentService.delete(commentId, null, requestDto.password());
        return ResponseEntity.ok().build();
    }
}
