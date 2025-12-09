package com.mxxdone.miniproject.service;

import com.mxxdone.miniproject.domain.*;
import com.mxxdone.miniproject.dto.comment.CommentResponseDto;
import com.mxxdone.miniproject.dto.comment.CommentSaveRequestDto;
import com.mxxdone.miniproject.dto.comment.CommentUpdateRequestDto;
import com.mxxdone.miniproject.dto.event.NotificationEvent;
import com.mxxdone.miniproject.repository.CommentRepository;
import com.mxxdone.miniproject.repository.PostRepository;
import com.mxxdone.miniproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;

    // 댓글 생성
    public Long save(CommentSaveRequestDto requestDto, String username) {
        Post post = postRepository.findById(requestDto.postId())
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));

        Comment.CommentBuilder commentBuilder = Comment.builder()
                .content(requestDto.content())
                .post(post);

        // username이 null이 아니면 (로그인 사용자)
        User currentUser = null;
        if (username != null) {
            currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            commentBuilder.author(currentUser);
        }
        // username이 null이면 (비로그인 사용자)
        else {
            commentBuilder.guestName(requestDto.guestName());
            // guest 사용자 비밀번호 암호화 후 저장
            commentBuilder.guestPassword(passwordEncoder.encode(requestDto.guestPassword()));
        }

        // 부모 댓글 유무 확인
        Comment parent = null;
        if (requestDto.parentId() != null) {
            parent = commentRepository.findById(requestDto.parentId())
                    .orElseThrow(() -> new NoSuchElementException("상위 댓글을 찾을 수 없습니다."));
            commentBuilder.parent(parent);
        }

        Comment comment = commentBuilder.build();
        Comment savedComment = commentRepository.save(comment);
        redisTemplate.delete("posts::page_1");

        // 알림 발송 메서드 호출
        publishNotification(post, savedComment, parent, currentUser);

        return savedComment.getId();
    }

    // 알림 발행 로직
    private void publishNotification(Post post, Comment savedComment, Comment parent, User currentUser) {
        try {
            User targetUser;
            String message;

            if (parent != null) {
                targetUser = parent.getAuthor();
                message = "회원님의 댓글에 답글이 달렸습니다.";
            } else { // 부모가 없는 글에 바로 쓰는 댓글
                targetUser = post.getAuthor();
                message = "회원님의 게시글에 댓글이 달렸습니다.";
            }
            // 본인 글에 쓴 댓글 알림 제외
            // 비로그인 사용자가 쓴 댓글은 무조건 알림 발송
            boolean isSelf = (currentUser != null && targetUser != null &&
                                currentUser.getId().equals(targetUser.getId()));

            // 수신자가 존재하고, 본인이 아닌 경우에 알림 발송
            if (targetUser != null && !isSelf) {
                // 해당 댓글 바로가기 기능을을 위한 앵커 추가
                Category childCategory = post.getCategory();
                Category parentCategory = childCategory.getParent();
                String parentSlug = (parentCategory != null) ? parentCategory.getSlug() : "category";
                String childSlug = childCategory.getSlug();

                String url = "/" + parentSlug + "/" + childSlug + "/posts/" + post.getId() + "#comment-" + savedComment.getId();                eventPublisher.publishEvent(new NotificationEvent(
                        targetUser.getId(),
                        message,
                        url,
                        NotificationType.COMMENT
                ));
            }
        } catch (Exception e) {
            log.error("알림 발송 실패: {}", e.getMessage());
        }
    }

    // 특정 게시글 댓글 조회
    @Transactional(readOnly = true)
    public List<CommentResponseDto> findByPostId(Long postId) {
        List<Comment> roots = commentRepository.findVisibleCommentsByPostId(postId);

        return roots.stream()
                .map(CommentResponseDto::from)
                .toList();
    }

    // 댓글 수정
    public void update(Long commentId, CommentUpdateRequestDto requestDto, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("해당 댓글이 없습니다."));
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 권한 확인
        if (currentUser.getRole() != Role.ADMIN && !comment.getAuthor().getUsername().equals(username)) {
            throw new AccessDeniedException("댓글을 수정할 권한이 없습니다.");
        }
        comment.update(requestDto.content());
    }

    // 댓글 삭제
    public void delete(Long commentId, String username, String password) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("해당 댓글이 없습니다."));

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
            throw new AccessDeniedException("댓글을 삭제할 권한이 없습니다.");
        }

        comment.softDelete();

        redisTemplate.delete("posts::page_1");
    }
}
