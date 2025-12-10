package com.mxxdone.miniproject.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT 토큰 응답 DTO")
public record TokenResponseDto(@Schema(description = "Access Token (Bearer)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String accessToken) {
}
