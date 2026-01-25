package com.mxxdone.miniproject.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE comment SET is_deleted = true WHERE id = ?") // delete 쿼리 대신 실행될 SQL
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // 비로그인 사용자를 위해 nullable = false 제거
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User author;

    @Column
    private String authorUsername;

    @Column
    private String authorNickname;

    // 비로그인 사용자명
    @Column
    private String guestName;

    @Column
    private String guestPassword;

    // soft delete
    @Column(nullable = false)
    private boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<Comment> children = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Builder
    public Comment(String content, Post post, User author, String guestName, String guestPassword, Comment parent) {
        this.content = content;
        this.post = post;
        this.author = author;
        this.guestName = guestName;
        this.guestPassword = guestPassword;
        this.parent = parent;
        // 스냅샷 로직 추가
        if (author != null) {
            this.authorNickname = author.getNickname();
            this.authorUsername = author.getUsername();
        }
        // author = null -> 게스트 회원
    }

    // 수정 편의 메서드
    public void update(String content) {
        this.content = content;
    }

    // soft delete 메서드
    public void softDelete() {
        this.isDeleted = true;
    }
}
