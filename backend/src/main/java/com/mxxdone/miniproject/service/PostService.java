package com.mxxdone.miniproject.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mxxdone.miniproject.domain.Category;
import com.mxxdone.miniproject.domain.Post;
import com.mxxdone.miniproject.domain.Role;
import com.mxxdone.miniproject.domain.User;
import com.mxxdone.miniproject.dto.PageDto;
import com.mxxdone.miniproject.dto.post.*;
import com.mxxdone.miniproject.repository.CategoryRepository;
import com.mxxdone.miniproject.repository.PostRepository;
import com.mxxdone.miniproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    // 게시글 저장
    @CacheEvict(value = "categories", allEntries = true)
    public PostSaveResponseDto save(PostSaveRequestDto requestDto, String username) {
        Category category = categoryRepository.findById(requestDto.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 없습니다. id= " + requestDto.categoryId()));

        if (!category.getChildren().isEmpty()) {
            throw new IllegalArgumentException("하위 카테고리에만 게시글을 작성할 수 있습니다.");
        }

        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 빌더를 이용하여 Post 객체 생성
        Post post = Post.builder()
                .title(requestDto.title())
                .content(requestDto.content())
                .category(category)
                .author(author)
                .build();

        Post savedPost =  postRepository.save(post);

        // 새로운 글이 작성되었으면 1페이지 캐시 삭제
        redisTemplate.delete("posts::page_1");

        return PostSaveResponseDto.from(savedPost);
    }

    //게시글 수정
    @CacheEvict(value = "categories", allEntries = true)
    public Long update(Long id, PostUpdateRequestDto requestDto, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id= " + id));

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (post.getAuthor() != null && !post.getAuthor().equals(currentUser) && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("게시글 수정 권한이 없습니다.");
        }

        post.update(requestDto.title(), requestDto.content());

        redisTemplate.delete("posts::page_1");
        log.info("캐시 정리: posts::page_1");
        return id;
    }

    //게시글 삭제
    @CacheEvict(value = "categories", allEntries = true)
    public void delete(Long id, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id= " + id));

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (post.getAuthor() != null && !post.getAuthor().equals(currentUser) && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("게시글 삭제 권한이 없습니다.");
        }

        postRepository.delete(post);

        redisTemplate.delete("posts::page_1");
        log.info("캐시 정리: posts::page_1");
    }

    //게시글 단건 조회
    //밖으로 나가는 데이터는 DTO로 변환하여 엔티티를 보호
    @Transactional(readOnly = true) //조회 기능은 readOnly = true 옵션으로 성능 최적화
    public PostDetailResponseDto findById(Long id) {
        Post entity = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id= " + id));

        return PostDetailResponseDto.from(entity);
    }

    // 게시글 목록 조회
    @Transactional(readOnly = true)
    public PageDto<PostSummaryResponseDto> findPosts(Long categoryId, String searchType, String keyword, Pageable pageable) {

        // 1페이지 목록에만 캐싱 적용
        boolean isCachable = pageable.getPageNumber() == 0 && categoryId == null && (keyword == null || keyword.isEmpty());
        final String cacheKey = "posts::page_1";

        if (isCachable) {
            try {
                // Redis에서 데이터 찾기
                String cachedData = redisTemplate.opsForValue().get(cacheKey);

                // 캐시에 데이터가 있으면
                if (cachedData != null) {
                    log.info("캐시 발견: 레디스로부터 게시물 1페이지 불러오기");
                    // JSON 문자열을 PageDto로 변환
                    PageDto<PostSummaryResponseDto> cachedPageDto = objectMapper.readValue(cachedData, new TypeReference<>() {});
                    // PageDto를 다시 Page 객체로 변환 후 반환
                    return cachedPageDto;
                }
            } catch (Exception e) {
                log.error("캐시 불러오기 실패", e);
            }
        }
        // 캐시를 사용하지 않거나 캐시를 찾지 못할 경우 DB에서 조회
        log.info("캐시 미발견: DB에서 조회");
        List<Long> categoryIds = null;
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() ->new IllegalArgumentException("해당 카테고리를 찾을 수 없습니다."));
            categoryIds = category.getDescendantIdsAndSelf();
        }
        Page<PostSummaryResponseDto> resultFromDb = postRepository.findPostsWithConditions(categoryIds, searchType, keyword, pageable);
        // DB에서 조회한 Page 객체를 PageDto로 변환
        PageDto<PostSummaryResponseDto> resultDto = PageDto.from(resultFromDb);

        // 캐싱 조건에 부합 -> 조회 결과를 Redis에 저장
        if (isCachable) {
            try {
                // PageDto를 JSON으로 변환 후 Redis에 저장
                redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(resultDto));
                log.info("캐시 생성: 게시글 1페이지 redis에 저장");
            } catch (Exception e) {
                log.error("캐시 생성 실패", e);
            }
        }

        return resultDto;
    }
}
