package com.mxxdone.miniproject.controller;

import com.mxxdone.miniproject.dto.admin.AdminSummaryResponseDto;
import com.mxxdone.miniproject.dto.admin.DailyStatResponseDto;
import com.mxxdone.miniproject.dto.admin.PopularPostResponseDto;
import com.mxxdone.miniproject.dto.admin.RecentCommentResponseDto;
import com.mxxdone.miniproject.service.AdminStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
@Tag(name = "관리자 통계 API", description = "관리자 대시보드용 통계를 제공합니다.")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @Operation(summary = "대시보드 요약 통계", description = "전체 및 오늘 기준의 게시글, 사용자, 댓글 수를 집계하여 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음")
    })
    @GetMapping("/summary")
    public ResponseEntity<AdminSummaryResponseDto> getSummary() {
        return ResponseEntity.ok(adminStatsService.getSummary());
    }

    @Operation(summary = "인기 게시글", description = "조회수를 기준으로 가장 인기 있는 게시글 목록을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음")
    })
    @GetMapping("/popular-posts")
    public ResponseEntity<List<PopularPostResponseDto>> getPopularPosts(
            @Parameter(description = "조회할 게시글 개수 (기본값: 5, 최대: 20)", example = "5")
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(adminStatsService.getPopularPosts(limit));
    }

    @Operation(summary = "최근 댓글 목록", description = "가장 최근에 작성된 댓글 목록과 해당 댓글이 달린 게시글 정보를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음")
    })
    @GetMapping("/recent-comments")
    public ResponseEntity<List<RecentCommentResponseDto>> getRecentComments(
            @Parameter(description = "조회할 댓글 개수 (기본값: 10, 최대: 50)", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(adminStatsService.getRecentComments(limit));
    }

    @Operation(summary = "일별 댓글 수 추이", description = "최근 N일 동안의 일별 댓글 작성 수를 날짜별로 그룹화하여 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음")
    })
    @GetMapping("/comments-daily")
    public ResponseEntity<List<DailyStatResponseDto>> getCommentsDailyStats(
            @Parameter(description = "조회할 최근 일수 (기본값: 30, 최대: 365)", example = "30")
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(adminStatsService.getCommentsDailyStats(days));
    }
}
