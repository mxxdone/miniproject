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
import java.util.Objects;

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

        // 엔티티의 빌더에서 사용자명, 닉네임 스냅샷 저장
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
        Category childCategory = post.getCategory();
        Category parentCategory = childCategory.getParent();
        String parentSlug = (parentCategory != null) ? parentCategory.getSlug() : "category";
        String childSlug = childCategory.getSlug();

        // 앵커(#comment-id)를 통해 해당 댓글로 바로 이동
        String url = "/" + parentSlug + "/" + childSlug + "/posts/" + post.getId() + "#comment-" + savedComment.getId();

        // 게시글 작성자에게 알림
        User postAuthor = post.getAuthor();
        boolean isPostOwnerSelf = (currentUser != null && postAuthor.equals(currentUser));

        // 부모 댓글 작성자 - 게시글 작성자 동일 여부
        boolean isParentAuthorSameAsPostAuthor = (parent != null && parent.getAuthor().equals(postAuthor));

        // 본인이 쓴 글이 아니거나, 부모 댓글 작성자가 해당 글 작성자가 아닌 경우
        // -> 작성자가 아닌 회원이 댓글을 쓴 경우
        if (!isPostOwnerSelf && !isParentAuthorSameAsPostAuthor) {
            sendNotification(postAuthor, "회원님의 게시글에 새로운 댓글이 달렸습니다.", url, NotificationType.COMMENT);
        }

        // 부모 댓글 작성자에게 알림 - 대댓글일 경우만
        if (parent != null) {
            User parentAuthor = parent.getAuthor();
            // 부모 - 자식 댓글 작성자 동일인 여부
            boolean isParentOwnerSelf = (currentUser != null && parentAuthor.equals(currentUser));

            // 서로 다르면 부모 댓글 작성자에게 알림
            if (parentAuthor != null && !isParentOwnerSelf) {
                sendNotification(parentAuthor, "회원님의 댓글에 답글이 달렸습니다.", url, NotificationType.COMMENT);
            }
        }
    }

    // 이벤트 전송(발행)
    private void sendNotification(User receiver, String content, String url, NotificationType type) {
        try {
            eventPublisher.publishEvent(new NotificationEvent(
                    receiver.getId(),
                    content,
                    url,
                    type
            ));
        } catch (Exception e) {
            log.error("알림 이벤트 발행 실패: receiverId={}, reason={}", receiver.getId(), e.getMessage());
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

        String authorUsername = comment.getAuthorUsername();
        // 권한 확인
        if (currentUser.getRole() != Role.ADMIN && !Objects.equals(authorUsername, username)) {
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
            String authorUsername = comment.getAuthorUsername();
            // admin이거나 본인 댓글일 경우 권한 있음
            if (currentUser != null && (currentUser.getRole() == Role.ADMIN || Objects.equals(authorUsername, username))) {
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
