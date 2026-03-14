package com.mxxdone.miniproject.service;

import com.mxxdone.miniproject.domain.Category;
import com.mxxdone.miniproject.domain.Post;
import com.mxxdone.miniproject.domain.Role;
import com.mxxdone.miniproject.domain.User;
import com.mxxdone.miniproject.repository.CategoryRepository;
import com.mxxdone.miniproject.repository.PostRepository;
import com.mxxdone.miniproject.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 실제 스프링 컨텍스트를 모두 띄웁니다 (실제 Redis, 진짜 DB 사용)
@SpringBootTest
@ActiveProfiles("test")
class PostServiceConcurrencyTest {

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

    // Mock이 아닌 진짜 RedisTemplate을 주입
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private Long targetPostId;

    @BeforeEach
    void setUp() {
        // 1. 아무것도 없는 텅 빈 H2 DB에 테스트용 유저 생성
        User testUser = userRepository.save(User.builder()
                .username("test_user")
                .password(passwordEncoder.encode("password"))
                .nickname("일반유저")
                .email("user@test.com")
                .role(Role.USER)
                .build()
        );

        // 2. 단말 카테고리 생성 (게시글 작성 제약 조건 통과용)
        Category testCategory = categoryRepository.save(
                new Category("테스트카테고리", "test-cat", 1)
        );

        // 3. 테스트 대상 게시글 생성
        Post testPost = postRepository.save(Post.builder()
                .title("동시성 테스트 게시글")
                .content("내용입니다.")
                .author(testUser)
                .category(testCategory)
                .build()
        );

        targetPostId = testPost.getId();
    }

    @AfterEach
    void tearDown() {
        // H2 DB 환경이므로 내 로컬 DB 망가질 걱정 없이 시원하게 테이블 전체 날림
        postRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        // 다음 테스트 실행에 영향 주지 않게 Redis 키도 날림
        redisTemplate.delete("post:view:" + targetPostId + ":192.168.0.1");
    }

    @Test
    @DisplayName("Redis 방어 테스트: 동일한 IP로 100명이 동시에 조회하면 조회수는 딱 1만 증가해야 한다")
    void incrementViewCount_sameIp_concurrency_test() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch readyLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.0.1"); // 모두 같은 IP

        Post initialPost = postRepository.findById(targetPostId).orElseThrow();
        long initialViewCount = initialPost.getViewCount();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    readyLatch.await();
                    postService.incrementViewCount(targetPostId, request);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        readyLatch.countDown();
        finishLatch.await();

        Post finalPost = postRepository.findById(targetPostId).orElseThrow();

        // 검증: 100번이 동시에 들어와도 setIfAbsent 덕분에 단 1번만 통과해야 함
        assertEquals(initialViewCount + 1, finalPost.getViewCount());
    }

    @Test
    @DisplayName("DB 방어 테스트: 서로 다른 IP를 가진 100명이 동시에 조회하면 조회수가 정확히 100 증가해야 한다")
    void incrementViewCount_differentIp_concurrency_test() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch readyLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        Post initialPost = postRepository.findById(targetPostId).orElseThrow();
        long initialViewCount = initialPost.getViewCount();

        for (int i = 0; i < threadCount; i++) {
            final int ipSuffix = i; // 각 스레드마다 다른 IP 부여
            executorService.submit(() -> {
                try {
                    MockHttpServletRequest request = new MockHttpServletRequest();
                    request.setRemoteAddr("192.168.0." + ipSuffix); // 192.168.0.0 ~ 192.168.0.99

                    readyLatch.await();
                    postService.incrementViewCount(targetPostId, request);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        readyLatch.countDown();
        finishLatch.await();

        Post finalPost = postRepository.findById(targetPostId).orElseThrow();

        // 검증: 서로 다른 IP이므로 Redis는 다 통과하지만, DB 쿼리(Update) 덕분에 손실 없이 100이 모두 더해져야 함
        assertEquals(initialViewCount + 100, finalPost.getViewCount());
    }
}
