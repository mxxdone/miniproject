package com.mxxdone.miniproject.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본생성자 추가
@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE post SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false") // select 기본 조건
public class Post extends BaseTimeEntity {

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

    @Column
    private Long userId;

    @Column
    private String authorUsername;

    @Column
    private String authorNickname;

    @OneToMany(mappedBy = "post")
    private List<Comment> comments = new ArrayList<>();

    @Column(length = 500)
    private String thumbnailUrl;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @Builder
    public Post(String title, String content, Category category, Long userId, String authorUsername, String authorNickname, String thumbnailUrl) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.userId = userId;
        this.authorUsername = authorUsername;
        this.authorNickname = authorNickname;
        this.thumbnailUrl = thumbnailUrl;
    }

    // 수정 편의 메서드 추가
    public void update(String title, String content, Category category, String thumbnailUrl) {
        this.title = title;
        this.content = content;
        // category가 null이 아닐 경우에만 업데이트 (null이면 기존 카테고리 유지)
        if (category != null) {
            this.category = category;
        }
        this.thumbnailUrl = thumbnailUrl;
    }

    // updatePost에서 수정 유무 체크
    public boolean isNotModified(String title, String content, Long categoryId) {
        return Objects.equals(this.title, title) &&
                Objects.equals(this.content, content) &&
                (this.category != null && Objects.equals(this.category.getId(), categoryId));
    }
}
