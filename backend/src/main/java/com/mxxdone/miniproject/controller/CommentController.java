package com.mxxdone.miniproject.controller;

import com.mxxdone.miniproject.dto.comment.CommentResponseDto;
import com.mxxdone.miniproject.dto.comment.CommentSaveRequestDto;
import com.mxxdone.miniproject.dto.comment.CommentUpdateRequestDto;
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
                                            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(commentService.save(requestDto, userDetails.getUsername()));
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponseDto>> getCommentByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.findByPostIdOrderByCreatedAtAsc(postId));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        commentService.update(commentId, requestDto, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, @AuthenticationPrincipal UserDetails userDetails) {
        commentService.delete(commentId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
