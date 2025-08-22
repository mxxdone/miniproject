package com.mxxdone.miniproject.dto.post;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mxxdone.miniproject.config.XssSanitizer;

public record PostUpdateRequestDto(@JsonDeserialize(using = XssSanitizer.class) String title,
                                   @JsonDeserialize(using = XssSanitizer.class) String content) {
}
