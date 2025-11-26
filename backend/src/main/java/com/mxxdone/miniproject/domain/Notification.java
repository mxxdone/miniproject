package com.mxxdone.miniproject.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver; // 알림 수신자 (게시글 작성자)

    @Column(nullable = false)
    private String content; // 알림 내용

    @Column(nullable = false)
    private String url; // 클릭 시 이동할 링크 (e.g. /posts/1)

    @Column(nullable = false)
    private boolean isRead = false; // 읽음 여부

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType; // 알림 타입 (댓글, 좋아요)

    @CreatedDate
    private Instant createdAt;

    @Builder
    public Notification(User receiver, String content, String url, NotificationType notificationType) {
        this.receiver = receiver;
        this.content = content;
        this.url = url;
        this.notificationType = notificationType;
    }

    public void read() {
        this.isRead = true;
    }
}
