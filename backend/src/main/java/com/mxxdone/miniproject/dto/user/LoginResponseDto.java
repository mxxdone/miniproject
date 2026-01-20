package com.mxxdone.miniproject.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(hidden = true)
public record LoginResponseDto(
        String accessToken,
        String refreshToken,
        long maxAge
) {
}
