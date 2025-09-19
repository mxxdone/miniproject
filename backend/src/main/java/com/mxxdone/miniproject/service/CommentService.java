package com.mxxdone.miniproject.service;

import com.mxxdone.miniproject.domain.Comment;
import com.mxxdone.miniproject.domain.Post;
import com.mxxdone.miniproject.domain.Role;
import com.mxxdone.miniproject.domain.User;
import com.mxxdone.miniproject.dto.comment.CommentResponseDto;
import com.mxxdone.miniproject.dto.comment.CommentSaveRequestDto;
import com.mxxdone.miniproject.dto.comment.CommentUpdateRequestDto;
import com.mxxdone.miniproject.repository.CommentRepository;
import com.mxxdone.miniproject.repository.PostRepository;
import com.mxxdone.miniproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 댓글 생성
    public Long save(CommentSaveRequestDto requestDto, String username) {
        Post post = postRepository.findById(requestDto.postId())
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));

        Comment.CommentBuilder commentBuilder = Comment.builder()
                .content(requestDto.content())
                .post(post);

        // username이 null이 아니면 (로그인 사용자)
        if (username != null) {
            User author = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            commentBuilder.author(author);
        }
        // username이 null이면 (비로그인 사용자)
        else {
            commentBuilder.guestName(requestDto.guestName());
            // guest 사용자 비밀번호 암호화 후 저장
            commentBuilder.guestPassword(passwordEncoder.encode(requestDto.guestPassword()));
        }
        Comment comment = commentBuilder.build();

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
        List<Comment> roots = commentRepository.findRootsWithChildren(postId);

        return roots.stream()
                .map(CommentResponseDto::from)
                .filter(dto -> !dto.isDeleted() || !dto.children().isEmpty())
                .toList();
    }

    // 댓글 수정
    public void update(Long commentId, CommentUpdateRequestDto requestDto, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 없습니다."));
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 권한 확인
        if (currentUser.getRole() != Role.ADMIN && !comment.getAuthor().getUsername().equals(username)) {
            throw new IllegalStateException("댓글을 수정할 권한이 없습니다.");
        }
        comment.update(requestDto.content());
    }

    // 댓글 삭제
    public void delete(Long commentId, String username, String password) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 없습니다."));

        boolean isAuthorized = false;
        // 로그인 사용자
        if (username != null) {
            User currentUser = userRepository.findByUsername(username).orElse(null);
            // admin이거나 본인 댓글일 경우 권한 있음
            if (currentUser != null && (currentUser.getRole() == Role.ADMIN || comment.getAuthor().equals(currentUser))) {
                isAuthorized = true;
            }
        //비로그인 사용자의 경우
        } else if (comment.getAuthor() == null && password != null) {
            if (passwordEncoder.matches(password, comment.getGuestPassword())) {
                isAuthorized = true;
            }
        }

        // 권한 확인
        if (!isAuthorized) {
            throw new IllegalStateException("댓글을 삭제할 권한이 없습니다.");
        }

        comment.softDelete();
    }
}
