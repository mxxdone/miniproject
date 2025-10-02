package com.mxxdone.miniproject.dto;

import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

// 페이징 결과를 캐싱하기 위한 전용 DTO
public record PageDto<T>(
        List<T> content,
        int totalPages,
        long totalElements,
        int pageNumber,
        int pageSize
) {
    // Page 객체를 PageDto로 변환하는 정적 메서드
    public static <T> PageDto<T> from(Page<T> page) {
        return new PageDto<>(
                // 스프링이 반환하는 특수한 리스트를 직렬화에 안전한 표준 ArrayList로 변환
                new ArrayList<>(page.getContent()),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize()
        );
    }
}
