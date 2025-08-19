package com.mxxdone.miniproject.service;

import com.mxxdone.miniproject.domain.Comment;
import com.mxxdone.miniproject.domain.Post;
import com.mxxdone.miniproject.domain.User;
import com.mxxdone.miniproject.dto.comment.CommentResponseDto;
import com.mxxdone.miniproject.dto.comment.CommentSaveRequestDto;
import com.mxxdone.miniproject.dto.comment.CommentUpdateRequestDto;
import com.mxxdone.miniproject.repository.CommentRepository;
import com.mxxdone.miniproject.repository.PostRepository;
import com.mxxdone.miniproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 댓글 생성
    public Long save(CommentSaveRequestDto requestDto, String username) {
        Post post = postRepository.findById(requestDto.postId())
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .content(requestDto.content())
                .post(post)
                .author(author)
                .build();

        return commentRepository.save(comment).getId();
    }

    // 특정 게시글 댓글 조회
    @Transactional(readOnly = true)
    public List<CommentResponseDto> findByPostId(Long postId) {
        List<Comment> comments = commentRepository.findByPostId(postId);
        return comments.stream()
                .filter(comment -> comment.getParent() == null) // 최상위 댓글 조회
                .map(CommentResponseDto::from)
                .filter(dto -> !dto.isDeleted() || !dto.children().isEmpty())
                .collect(Collectors.toList());
    }

    // 댓글 수정
    public void update(Long commentId, CommentUpdateRequestDto requestDto, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 없습니다."));

        // 권한 확인
        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }
        comment.update(requestDto.content());
    }

    // 댓글 삭제
    public void delete(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 없습니다."));

        // 권한 확인
        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }

        comment.softDelete();
    }
}
