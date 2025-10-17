package com.mxxdone.miniproject.service;

import com.mxxdone.miniproject.domain.Category;
import com.mxxdone.miniproject.domain.Post;
import com.mxxdone.miniproject.domain.Role;
import com.mxxdone.miniproject.domain.User;
import com.mxxdone.miniproject.dto.post.PostSaveRequestDto;
import com.mxxdone.miniproject.dto.post.PostUpdateRequestDto;
import com.mxxdone.miniproject.repository.CategoryRepository;
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
class PostServiceTest {

    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;

    @MockitoBean
    private S3Uploader s3Uploader;

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testAdmin;
    private User testUser;
    private Category testCategory;
    private Post testPost;

    @BeforeEach
    void setUp() {
        testAdmin = userRepository.save(User.builder()
                .username("admin")
                .password(passwordEncoder.encode("password"))
                .nickname("관리자")
                .email("admin@test.com")
                .role(Role.ADMIN)
                .build()
        );
        testUser = userRepository.save(User.builder()
                .username("user")
                .password(passwordEncoder.encode("password"))
                .nickname("일반유저")
                .email("user@test.com")
                .role(Role.USER)
                .build()
        );
        testCategory = categoryRepository.save(new Category("테스트 카테고리", "test-category", 10));
        testPost = postRepository.save(Post.builder()
                .title("테스트 게시글")
                .content("내용입니다.")
                .author(testUser)
                .category(testCategory)
                .build()
        );
    }

    @Test
    @DisplayName("정상적인 정보로 게시글을 작성하면 성공")
    void savePost_success() {
        // given
        PostSaveRequestDto requestDto = new PostSaveRequestDto("게시글 제목", "게시글 내용", testCategory.getId());

        // when
        Long postId = postService.save(requestDto, testAdmin.getUsername());

        // then
        assertThat(postId).isNotNull();
        Post savedPost = postRepository.findById(postId).orElseThrow();
        assertThat(savedPost.getTitle()).isEqualTo("게시글 제목");
        assertThat(savedPost.getAuthor().getUsername()).isEqualTo("admin");
    }

    @Test
    @DisplayName("게시글 작성자가 자신의 글을 수정하면 성공")
    void updatePost_success_by_author() {
        // given
        PostUpdateRequestDto requestDto = new PostUpdateRequestDto("수정된 제목", "수정된 내용");

        // when
        postService.update(testPost.getId(), requestDto, testUser.getUsername());

        // then
        Post updatedPost = postRepository.findById(testPost.getId()).orElseThrow();
        assertThat(updatedPost.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedPost.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("관리자는 다른 사람의 글을 수정 가능")
    void updatePost_success_by_admin() {
        // given
        PostUpdateRequestDto requestDto = new PostUpdateRequestDto("관리자가 수정한 제목", "관리자가 수정한 내용");

        // when
        postService.update(testPost.getId(), requestDto, testAdmin.getUsername());

        // then
        Post updatedPost = postRepository.findById(testPost.getId()).orElseThrow();
        assertThat(updatedPost.getTitle()).isEqualTo("관리자가 수정한 제목");
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 글 수정을 시도하면 AccessDeniedException이 발생")
    void updatePost_fail_by_not_author() {
        // given
        User otherUser = userRepository.save(User.builder().username("otherUser").password(passwordEncoder.encode("password")).nickname("다른유저").email("other@test.com").role(Role.USER).build());
        PostUpdateRequestDto requestDto = new PostUpdateRequestDto("수정 시도", "수정 시도");

        // when & then
        assertThatThrownBy(() -> postService.update(testPost.getId(), requestDto, otherUser.getUsername()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("게시글 수정 권한이 없습니다.");
    }

    @Test
    @DisplayName("게시글 작성자가 자신의 글을 삭제하면 성공")
    void deletePost_success_by_author() {
        // when
        postService.delete(testPost.getId(), testUser.getUsername());

        // then
        assertThat(postRepository.findById(testPost.getId())).isEmpty();
    }

    @Test
    @DisplayName("관리자는 다른 사람의 글을 삭제 가능")
    void deletePost_success_by_admin() {
        // when
        postService.delete(testPost.getId(), testAdmin.getUsername());

        // then
        assertThat(postRepository.findById(testPost.getId())).isEmpty();
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 글 삭제를 시도하면 AccessDeniedException이 발생")
    void deletePost_fail_by_not_author() {
        // given
        User otherUser = userRepository.save(User.builder().username("otherUser2").password(passwordEncoder.encode("password")).nickname("다른유저2").email("other2@test.com").role(Role.USER).build());

        // when & then
        assertThatThrownBy(() -> postService.delete(testPost.getId(), otherUser.getUsername()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("게시글 삭제 권한이 없습니다.");
    }
}