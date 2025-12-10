package com.mxxdone.miniproject.dto.notification;

import com.mxxdone.miniproject.domain.Notification;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "알림 응답 DTO")
public record NotificationResponseDto(
        @Schema(description = "알림 ID", example = "50")
        Long id,

        @Schema(description = "알림 메시지", example = "회원님의 게시글에 댓글이 달렸습니다.")
        String content,

        @Schema(description = "이동할 URL", example = "/backend/security/posts/1#comment-5")
        String url,

        @Schema(description = "읽음 여부", example = "false")
        boolean isRead,

        @Schema(description = "알림 타입 (COMMENT, LIKE 등)", example = "COMMENT")
        String type,

        @Schema(description = "알림 생성 일시")
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
