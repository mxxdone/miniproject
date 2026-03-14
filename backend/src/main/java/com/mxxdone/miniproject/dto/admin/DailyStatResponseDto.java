package com.mxxdone.miniproject.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "일별 통계 응답 DTO")

public record DailyStatResponseDto(
        @Schema(description = "기준 날짜", example = "2025-12-25")
        LocalDate date,

        @Schema(description = "해당 날짜의 집계 수치", example = "15")
        Long count
) {}
