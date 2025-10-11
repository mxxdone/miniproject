package com.mxxdone.miniproject.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mxxdone.miniproject.domain.*;
import com.mxxdone.miniproject.dto.comment.CommentSaveRequestDto;
import com.mxxdone.miniproject.repository.CategoryRepository;
import com.mxxdone.miniproject.repository.CommentRepository;
import com.mxxdone.miniproject.repository.PostRepository;
import com.mxxdone.miniproject.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private S3Client s3Client;

    private User testUser;
    private Post testPost;
    private Comment testUserComment;
    private Comment testGuestComment;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자, 게시글, 댓글 데이터 미리 생성
        userRepository.save(User.builder()
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
                .role(Role.USER).build()
        );

        Category testCategory = categoryRepository.save(new Category("테스트 카테고리"));
        testPost = postRepository.save(Post.builder()
                .title("테스트 게시글")
                .content("내용입니다.")
                .author(testUser)
                .category(testCategory)
                .build()
        );

        testUserComment = commentRepository.save(Comment.builder()
                .content("로그인 사용자 댓글")
                .post(testPost)
                .author(testUser)
                .build()
        );

        testGuestComment = commentRepository.save(Comment.builder()
                .content("게스트 댓글")
                .post(testPost)
                .guestName("guest")
                .guestPassword(passwordEncoder.encode("guest1234"))
                .build()
        );
    }

    @Test
    @DisplayName("로그인한 사용자가 댓글을 작성하면 성공(200 OK)")
    @WithMockUser(username = "user", roles = "USER")
    void saveComment_success_by_loggedInUser() throws Exception {
        CommentSaveRequestDto requestDto = new CommentSaveRequestDto(testPost.getId(), "로그인 사용자 댓글", null, null, null);
        String requestBody = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/api/v1/comments").contentType(MediaType.APPLICATION_JSON).content(requestBody).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게스트가 댓글을 작성하면 성공(200 OK)")
    void saveComment_success_by_guest() throws Exception {
        CommentSaveRequestDto requestDto = new CommentSaveRequestDto(testPost.getId(), "게스트 댓글", null, "guest", "guest1234");
        String requestBody = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/api/v1/comments").contentType(MediaType.APPLICATION_JSON).content(requestBody).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("댓글 작성자가 자신의 댓글을 삭제하면 성공(200 OK)")
    @WithMockUser(username = "user", roles = "USER")
    void deleteComment_success_by_author() throws Exception {
        mockMvc.perform(delete("/api/v1/comments/" + testUserComment.getId()).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("관리자는 다른 사람의 댓글을 삭제 가능")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteComment_success_by_admin() throws Exception {
        mockMvc.perform(delete("/api/v1/comments/" + testUserComment.getId()).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게스트가 올바른 비밀번호로 댓글을 삭제하면 성공(200 OK)")
    void deleteComment_success_by_guest() throws Exception {
        // given : 비밀번호를 JSON 형식으로 준비
        String requestBody = "{\"password\":\"guest1234\"}"; // JSON 객체 형태로 변경

        // when & then
        mockMvc.perform(delete("/api/v1/comments/" + testGuestComment.getId() + "/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게스트가 틀린 비밀번호로 댓글 삭제를 시도하면 실패(403 Forbidden).")
    void deleteComment_fail_by_guest_wrong_password() throws Exception {
        // give
        String requestBody = "{\"password\":\"wrong-password\"}";

        // when & then
        mockMvc.perform(delete("/api/v1/comments/" + testGuestComment.getId() + "/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }
}