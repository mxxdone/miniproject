package com.mxxdone.miniproject.service;

import com.mxxdone.miniproject.domain.*;
import com.mxxdone.miniproject.dto.comment.CommentSaveRequestDto;
import com.mxxdone.miniproject.dto.comment.CommentUpdateRequestDto;
import com.mxxdone.miniproject.repository.CategoryRepository;
import com.mxxdone.miniproject.repository.CommentRepository;
import com.mxxdone.miniproject.repository.PostRepository;
import com.mxxdone.miniproject.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class CommentServiceTest {

    @Autowired
    private CommentService commentService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private S3Uploader s3Uploader;
    @MockitoBean
    RedisTemplate<String, String> redisTemplate;

    private User testUser;
    private User testAdmin;
    private Post testPost;
    private Comment testComment; // 수정/삭제 대상이 될 댓글 변수 추가
    private Comment testGuestComment;

    @BeforeEach
    void setUp() {
        // 테스트에 필요한 사용자, 카테고리, 게시글을 미리 생성
        testUser = userRepository.save(User.builder()
                .username("user")
                .password(passwordEncoder.encode("password"))
                .nickname("일반유저")
                .email("user@test.com")
                .role(Role.USER)
                .build());

        testAdmin = userRepository.save(User.builder()
                .username("admin")
                .password(passwordEncoder.encode("password"))
                .nickname("관리자")
                .email("admin@test.com")
                .role(Role.ADMIN)
                .build()
        );

        Category testCategory = categoryRepository.save(new Category("테스트 카테고리", "test-category", 10));

        testPost = postRepository.save(Post.builder()
                .title("테스트 게시글")
                .content("내용입니다.")
                .author(testUser)
                .category(testCategory)
                .build()
        );

        testComment = commentRepository.save(Comment.builder()
                .content("원본 댓글")
                .post(testPost)
                .author(testUser)
                .build()
        );

        // 게스트가 작성한 댓글
        testGuestComment = commentRepository.save(Comment.builder()
                .content("게스트 댓글")
                .post(testPost)
                .guestName("guest")
                .guestPassword(passwordEncoder.encode("guest1234")) // 비밀번호 암호화하여 저장
                .build()
        );
    }

    @Test
    @DisplayName("로그인한 사용자가 댓글을 작성하면 성공")
    void saveComment_success_by_loggedInUser() {
        // given
        CommentSaveRequestDto requestDto = new CommentSaveRequestDto(
                testPost.getId(),
                "테스트 댓글입니다.",
                null, // 최상위 댓글
                null, // 게스트 이름 없음
                null  // 게스트 비밀번호 없음
        );

        // when
        Long commentId = commentService.save(requestDto, testUser.getUsername());

        // then
        assertThat(commentId).isNotNull();

        Comment savedComment = commentRepository.findById(commentId).orElseThrow();
        assertThat(savedComment.getContent()).isEqualTo("테스트 댓글입니다.");
        assertThat(savedComment.getAuthor().getUsername()).isEqualTo("user");
        assertThat(savedComment.getPost().getId()).isEqualTo(testPost.getId());
    }

    @Test
    @DisplayName("댓글 작성자가 자신의 댓글을 수정하면 성공")
    void updateComment_success_by_author() {
        // given
        CommentUpdateRequestDto requestDto = new CommentUpdateRequestDto("수정된 댓글 내용");

        // when
        commentService.update(testComment.getId(), requestDto, testUser.getUsername());

        // then
        Comment updatedComment = commentRepository.findById(testComment.getId()).orElseThrow();
        assertThat(updatedComment.getContent()).isEqualTo("수정된 댓글 내용");
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 댓글 수정을 시도하면 AccessDeniedException 발생")
    void updateComment_fail_by_not_author() {
        // given
        User otherUser = userRepository.save(User.builder()
                .username("otherUser")
                .password(passwordEncoder.encode("password"))
                .nickname("다른유저")
                .email("other@test.com")
                .role(Role.USER)
                .build()
        );
        CommentUpdateRequestDto requestDto = new CommentUpdateRequestDto("수정 시도");

        // when & then
        assertThatThrownBy(() -> commentService.update(testComment.getId(), requestDto, otherUser.getUsername()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("댓글을 수정할 권한이 없습니다.");
    }

    @Test
    @DisplayName("댓글 작성자가 자신의 댓글을 삭제하면 성공")
    void deleteComment_success_by_author() {
        // when
        commentService.delete(testComment.getId(), testUser.getUsername(), null);

        // then
        Comment deletedComment = commentRepository.findById(testComment.getId()).orElseThrow();
        assertThat(deletedComment.isDeleted()).isTrue(); // soft delete 확인
    }

    @Test
    @DisplayName("게스트가 올바른 비밀번호로 자신의 댓글 삭제 시도하면 성공")
    void deleteComment_success_by_guest() {
        // when
        commentService.delete(testGuestComment.getId(), null, "guest1234");

        // then
        Comment deletedComment = commentRepository.findById(testGuestComment.getId()).orElseThrow();
        assertThat(deletedComment.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("게스트가 틀린 비밀번호로 댓글 삭제 시도 시 AccessDeniedException 발생")
    void deleteComment_fail_by_guest_wrong_password() {
        // when & then
        assertThatThrownBy(() -> commentService.delete(testGuestComment.getId(), null, "wrong_password"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("댓글을 삭제할 권한이 없습니다.");
    }

    @Test
    @DisplayName("관리자는 다른 사람의 댓글 삭제 가능")
    void deleteComment_success_by_admin() {
        // when
        commentService.delete(testComment.getId(), testAdmin.getUsername(), null);

        // then
        Comment deletedComment = commentRepository.findById(testComment.getId()).orElseThrow();
        assertThat(deletedComment.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 댓글 삭제를 시도하면 AccessDeniedException 발생")
    void deleteComment_fail_by_not_author() {
        // given
        User otherUser = userRepository.save(User.builder().username("otherUser").password(passwordEncoder.encode("password")).nickname("다른유저").email("other@test.com").role(Role.USER).build());

        // when & then
        assertThatThrownBy(() -> commentService.delete(testComment.getId(), otherUser.getUsername(), null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("댓글을 삭제할 권한이 없습니다.");
    }
}