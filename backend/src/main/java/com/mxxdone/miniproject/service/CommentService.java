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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        if (requestDto.parentId() != null) {
            Comment parent = commentRepository.findById(requestDto.parentId())
                    .orElseThrow(() -> new IllegalArgumentException("상위 댓글을 찾을 수 없습니다."));
            comment.setParent(parent);
        }

        return commentRepository.save(comment).getId();
    }

    // 특정 게시글 댓글 조회
    @Transactional(readOnly = true)
    public List<CommentResponseDto> findByPostId(Long postId) {
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        List<CommentResponseDto> commentDtos = new ArrayList<>();
        Map<Long, CommentResponseDto> commentDtoMap = new HashMap<>();

        // 모든 댓글을 DTO로 변환 후 Map에 저장
        comments.forEach(comment -> {
            CommentResponseDto dto = CommentResponseDto.from(comment);
            commentDtoMap.put(dto.id(), dto);
            if (comment.getParent() == null) {
                commentDtos.add(dto); // 최상위 댓글은 바로 노출될 리스트로 추가
            }
        });

        // 대댓글들을 상위 댓글의 children 리스트에 추가
        comments.forEach(comment-> {
            if (comment.getParent() != null) {
                CommentResponseDto parentDto = commentDtoMap.get(comment.getParent().getId());
                // 상위 댓글 DTO의 children 리스트에 대댓글 DTO 추가
                if (parentDto != null) {
                    parentDto.children().add(commentDtoMap.get(comment.getId()));
                }
            }
        });

        // 삭제되었거나 대댓글 없는 댓글은 노출될 리스트에서 제외
        return commentDtos.stream()
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
