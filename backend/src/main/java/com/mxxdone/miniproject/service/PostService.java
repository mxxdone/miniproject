package com.mxxdone.miniproject.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mxxdone.miniproject.domain.*;
import com.mxxdone.miniproject.dto.PageDto;
import com.mxxdone.miniproject.dto.category.CategoryDto;
import com.mxxdone.miniproject.dto.post.*;
import com.mxxdone.miniproject.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    // 게시글 저장
    @CacheEvict(value = "categories", allEntries = true)
    public PostSaveResponseDto save(PostSaveRequestDto requestDto, User author) {
        Category category = categoryRepository.findById(requestDto.categoryId())
                .orElseThrow(() -> new NoSuchElementException("해당 카테고리가 없습니다. id= " + requestDto.categoryId()));

        if (!category.getChildren().isEmpty()) {
            throw new IllegalArgumentException("하위 카테고리에만 게시글을 작성할 수 있습니다.");
        }

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
    public Long update(Long id, PostUpdateRequestDto requestDto, User currentUser) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 게시글이 없습니다. id= " + id));

        if (post.getAuthor() != null && !post.getAuthor().equals(currentUser) && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("게시글 수정 권한이 없습니다.");
        }

        Category category = null;
        if (requestDto.categoryId() != null) {
            category = categoryRepository.findById(requestDto.categoryId())
                    .orElseThrow(() -> new NoSuchElementException("해당 카테고리가 없습니다. id= " + requestDto.categoryId()));
            // 하위 카테고리만 글 작성 가능
            if (!category.getChildren().isEmpty()) {
                throw new IllegalArgumentException("하위 카테고리에만 게시글을 작성할 수 있습니다.");
            }
        }

        post.update(requestDto.title(), requestDto.content(), category);

        redisTemplate.delete("posts::page_1");
        log.info("캐시 정리: posts::page_1");
        return id;
    }

    //게시글 삭제
    @CacheEvict(value = "categories", allEntries = true)
    public void delete(Long id, User currentUser) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 게시글이 없습니다. id= " + id));

        if (post.getAuthor() != null && !post.getAuthor().equals(currentUser) && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("게시글 삭제 권한이 없습니다.");
        }

        postRepository.delete(post);

        redisTemplate.delete("posts::page_1");
        log.info("캐시 정리: posts::page_1");
    }

    // 게시글 조회수 증가 (ip 기준 중복 방지)
    @Transactional(readOnly = false)
    public void incrementViewCount(Long id, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String redisKey = "post:view:" + id + ":" + ip;

        // Redis에 해당 ip로 조회한 기록이 있는지 확인
        if (!redisTemplate.hasKey(redisKey)) {
            // 기록이 없으면 24시간 동안 유효한 키를 redis에 저장
            redisTemplate.opsForValue().set(redisKey, "1", Duration.ofHours(24));

            // DB의 조회수 1 증가 (JPA dirty checkig 활용)
            Post post = postRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("해당 게시글이 없습니다."));
            post.incrementViewCount();
        }
    }

    //게시글 단건 조회
    //밖으로 나가는 데이터는 DTO로 변환하여 엔티티를 보호
    @Transactional(readOnly = true) //조회 기능은 readOnly = true 옵션으로 성능 최적화
    public PostDetailResponseDto findById(Long id) {
        // Post 엔티티 조회
        Post entity = postRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 게시글이 없습니다. id= " + id));

        // 카테고리 경로 계산
        List<CategoryDto> categoryPath = calculateCategoryPath(entity);

        // 현재 사용자의 게시글 좋아요 여부 계산
        boolean isLiked = checkLikedByCurrnetUser(entity);

        // 댓글 수 계산
        long commentCount = commentRepository.countByPostAndIsDeletedFalse(entity);

        // DTO 생성 및 반환
        return new PostDetailResponseDto(
                entity.getId(),
                entity.getTitle(),
                entity.getContent(),
                categoryPath,
                entity.getCategory() != null ? entity.getCategory().getId() : null,
                entity.getAuthor() != null ? entity.getAuthor().getUsername() : null,
                entity.getAuthor() != null ? entity.getAuthor().getNickname() : null,
                entity.getViewCount(),
                entity.getLikeCount(),
                isLiked,
                commentCount,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    // 현재 로그인 사용자가 해당 게시글에 좋아요 눌렀는지 확인
    private boolean checkLikedByCurrnetUser(Post post) {
        // SecurityContext에서 현재 인증 정보 확인
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // principal이 UserDetails의 인스턴스인지 확인 (로그인 상태)
        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            // DB에서 사용자 조회
            User user = userRepository.findByUsername(username).orElse(null);

            if (user != null) {
                // 사용자, 게시글을 가지고 좋아요 여부 체크
                return postLikeRepository.existsByUserAndPost(user, post);
            }
        }
        // 로그인X, 사용자를 찾을 수 없으면 false
        return false;
    }

    // Post 엔티티의 카테고리 정보를 바탕으로 상위 카테고리까지의 경로를 계산
    private List<CategoryDto> calculateCategoryPath(Post post) {
        List<CategoryDto> categoryPath = new ArrayList<>();
        if (post.getCategory() != null) {
            Category currentCategory = post.getCategory();
            while (currentCategory != null) {
                categoryPath.add(CategoryDto.from(currentCategory));
                currentCategory = currentCategory.getParent();
            }
            Collections.reverse(categoryPath); // 상위 -> 하위 카테고리 순서 변경
        }
        return categoryPath;
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
                    .orElseThrow(() ->new NoSuchElementException("해당 카테고리를 찾을 수 없습니다."));
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

    // 좋아요 토글
    @Transactional
    public void toggleLike(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("해당 게시글이 없습니다."));
        Optional<PostLike> existingLike = postLikeRepository.findByUserAndPost(user, post);

        if (existingLike.isPresent()) {
            // 이미 좋아요 누름 -> 취소
            postLikeRepository.delete(existingLike.get());
            post.decrementLikeCount();
        } else {
            // 좋아요 누르지 않음 -> 좋아요
            postLikeRepository.save(new PostLike(user, post));
            post.incrementLikeCount();
        }
        redisTemplate.delete("posts::page_1");
    }
}
