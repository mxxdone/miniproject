package com.mxxdone.miniproject.repository;

import com.mxxdone.miniproject.dto.post.PostSummaryResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {
    Page<PostSummaryResponseDto> findPostsWithConditions(Long categoryId, String searchType, String keyword, Pageable pageable);
}
