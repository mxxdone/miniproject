package com.mxxdone.miniproject.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본생성자 추가
@Getter @Setter
@Entity
@EntityListeners(AuditingEntityListener.class) // 추가
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // category 필드 값은 필수(not null)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    private String title;

    @Column(columnDefinition = "TEXT") // content는 내용이 길어질 수 있으므로 TEXT 타입으로 설정
    private String content;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // DTO를 위한 생성자 추가
    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // 수정 편의 메서드 추가
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
