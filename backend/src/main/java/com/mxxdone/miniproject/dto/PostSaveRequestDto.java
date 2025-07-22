package com.mxxdone.miniproject.dto;

import com.mxxdone.miniproject.domain.Post;

//게시글 생성 요청 DTO
public record PostSaveRequestDto(String title, String content) {

    public Post toEntity(){
        return new Post(title, content); // 타이틀과 컨텐츠를 갖고 post 객체 생성
    }
}

