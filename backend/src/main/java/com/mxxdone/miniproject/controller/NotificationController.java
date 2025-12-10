package com.mxxdone.miniproject.controller;

import com.mxxdone.miniproject.config.security.PrincipalDetails;
import com.mxxdone.miniproject.dto.notification.NotificationResponseDto;
import com.mxxdone.miniproject.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification API", description = "알림 관련 API (조회, 읽음 처리)")
public class NotificationController {

    private final NotificationService notificationService;

    // 알림 목록 조회
    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "로그인한 사용자의 읽지 않은 알림 목록을 최신순으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요")
    })
    public ResponseEntity<List<NotificationResponseDto>> getNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(notificationService.findAllByUsername(principalDetails.getUsername()));
    }

    // 안 읽은 알림 개수
    @GetMapping("/unread-count")
    @Operation(summary = "안 읽은 알림 개수", description = "읽지 않은 알림의 총 개수를 반환합니다. (뱃지 표시용)")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<Long> getUnreadCount(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(notificationService.countUnreadNotifications(principalDetails.getUsername()));
    }

    // 알림 읽음 처리
    @PatchMapping("/{id}/read")
    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 클릭했을 때 읽음 상태로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "403", description = "본인의 알림이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 알림")
    })
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "알림 ID", example = "1") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
        notificationService.markAsRead(id, principalDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all")
    @Operation(summary = "알림 전체 읽음 처리", description = "사용자의 모든 알림을 읽음 상태로 변경합니다.")
    @ApiResponse(responseCode = "200", description = "처리 성공")
    public ResponseEntity<Void> markAllAsRead(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails) {
        notificationService.markAllAsRead(principalDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
