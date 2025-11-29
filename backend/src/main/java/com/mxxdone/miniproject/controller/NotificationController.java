package com.mxxdone.miniproject.controller;

import com.mxxdone.miniproject.config.security.PrincipalDetails;
import com.mxxdone.miniproject.dto.notification.NotificationResponseDto;
import com.mxxdone.miniproject.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 알림 목록 조회
    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> getNotifications(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(notificationService.findAllByUsername(principalDetails.getUsername()));
    }

    // 안 읽은 알림 개수
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(notificationService.countUnreadNotifications(principalDetails.getUsername()));
    }

    // 알림 읽음 처리
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        notificationService.markAsRead(id, principalDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        notificationService.markAllAsRead(principalDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
