package com.mxxdone.miniproject.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 대시보드 요약 통계 응답 DTO")
public record AdminSummaryResponseDto(

        @Schema(description = "전체 게시글 수", example = "100")
        long totalPosts,

        @Schema(description = "전체 회원 수", example = "10")
        long totalUsers,

        @Schema(description = "전체 댓글 수", example = "200")
        long totalComments,

        @Schema(description = "오늘 가입한 신규 회원 수", example = "2")
        long todayNewUsers,

        @Schema(description = "오늘 작성된 게시글 수", example = "10")
        long todayPosts,

        @Schema(description = "오늘 작성된 댓글 수", example = "12")
        long todayComments
) {
}
