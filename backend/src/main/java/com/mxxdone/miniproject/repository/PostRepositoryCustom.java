package com.mxxdone.miniproject.repository;

import com.mxxdone.miniproject.dto.post.PostSummaryResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostRepositoryCustom {
    Page<PostSummaryResponseDto> findPostsWithConditions(List<Long> categoryIds, String searchType, String keyword, Pageable pageable);
}
