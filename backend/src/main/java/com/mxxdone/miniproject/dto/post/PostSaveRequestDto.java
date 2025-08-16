package com.mxxdone.miniproject.dto.post;

//게시글 생성 요청 DTO
public record PostSaveRequestDto(String title, String content, Long categoryId) {
}

