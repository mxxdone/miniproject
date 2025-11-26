package com.mxxdone.miniproject.dto.notification;

import com.mxxdone.miniproject.domain.Notification;

import java.time.Instant;

public record NotificationResponseDto(
        Long id,
        String content,
        String url,
        boolean isRead,
        String type, // 프론트에서 구분용 (COMMENT, LIKE)
        Instant createdAt
) {
    public static NotificationResponseDto from(Notification notification) {
        return new NotificationResponseDto(
                notification.getId(),
                notification.getContent(),
                notification.getUrl(),
                notification.isRead(),
                notification.getNotificationType().name(),
                notification.getCreatedAt()
        );
    }
}
