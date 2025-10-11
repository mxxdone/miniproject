package com.mxxdone.miniproject.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mxxdone.miniproject.domain.Category;
import com.mxxdone.miniproject.domain.Post;
import com.mxxdone.miniproject.domain.Role;
import com.mxxdone.miniproject.domain.User;
import com.mxxdone.miniproject.dto.post.PostSaveRequestDto;
import com.mxxdone.miniproject.dto.post.PostUpdateRequestDto;
import com.mxxdone.miniproject.repository.CategoryRepository;
import com.mxxdone.miniproject.repository.PostRepository;
import com.mxxdone.miniproject.repository.UserRepository;
import com.mxxdone.miniproject.service.S3Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private S3Uploader s3Uploader;
    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;

    private Long testCategoryId;
    private User  testUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        // H2 DB에 테스트용 데이터 미리 생성

        // 관리자 계정 생성
        userRepository.save(User.builder()
                .username("admin")
                .password(passwordEncoder.encode("password"))
                .nickname("관리자")
                .email("admin@test.com")
                .role(Role.ADMIN)
                .build());

        // 일반유저 생성
        testUser = userRepository.save(User.builder()
                .username("user")
                .password(passwordEncoder.encode("password"))
                .nickname("일반유저")
                .email("user@test.com")
                .role(Role.USER)
                .build());

        // 다른유저 생성
        userRepository.save(User.builder()
                .username("otherUser")
                .password(passwordEncoder.encode("password"))
                .nickname("다른유저")
                .email("other@test.com")
                .role(Role.USER)
                .build());

        // 카테고리 생성
        Category testCategory = categoryRepository.save(new Category("테스트 카테고리"));
        testCategoryId = testCategory.getId();

        // 수정/삭제 테스트에 사용될 게시글 작성자 'user'로 생성
        testPost = postRepository.save(Post.builder()
                .title("테스트 게시글")
                .content("내용입니다.")
                .author(testUser)
                .category(testCategory)
                .build());
    }

    @Test
    @DisplayName("ADMIN 권한으로 게시글 생성 요청 시 성공")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void savePost_success_with_admin() throws Exception {
        // given
        PostSaveRequestDto requestDto = new PostSaveRequestDto("테스트 제목", "테스트 내용", testCategoryId);
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("USER 권한으로 게시글 생성 요청 시 실패(403 Forbidden)")
    @WithMockUser(username = "user", roles = "USER")
    void savePost_fail_with_user() throws Exception {
        // given
        PostSaveRequestDto requestDto = new PostSaveRequestDto("테스트 제목", "테스트 내용", testCategoryId);
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("게시글 작성자가 자신의 글을 수정하면 성공")
    @WithMockUser(username = "user", roles = "USER")
    void updatePost_success_by_author() throws Exception {
        // given
        PostUpdateRequestDto requestDto = new PostUpdateRequestDto("수정된 제목", "수정된 내용");
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(put("/api/v1/posts/" + testPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("작성자가 아닌 다른 사용자가 글 수정을 시도하면 실패(403 Forbidden)")
    @WithMockUser(username = "otherUser", roles = "USER") // 'user'가 아닌 다른 사용자
    void updatePost_fail_by_not_author() throws Exception {
        // given
        PostUpdateRequestDto requestDto = new PostUpdateRequestDto("수정된 제목", "수정된 내용");
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(put("/api/v1/posts/" + testPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("게시글 작성자가 자신의 글을 삭제하면 성공")
    @WithMockUser(username = "user", roles = "USER")
    void deletePost_success_by_author() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/posts/" + testPost.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("작성자가 아닌 다른 사용자가 글 삭제를 시도하면 실패(403 Forbidden)")
    @WithMockUser(username = "otherUser", roles = "USER")
    void deletePost_fail_by_not_author() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/posts/" + testPost.getId()))
                .andExpect(status().isForbidden());
    }
}