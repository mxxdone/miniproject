package com.mxxdone.miniproject.dto.event;

import com.mxxdone.miniproject.domain.NotificationType;

public record NotificationEvent(
        Long receiverId,
        String content,
        String url,
        NotificationType notificationType
) {
}
