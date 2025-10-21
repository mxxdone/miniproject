package com.mxxdone.miniproject.dto.post;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mxxdone.miniproject.util.TitleSanitizer;
import com.mxxdone.miniproject.util.XssSanitizer;

public record PostUpdateRequestDto(@JsonDeserialize(using = TitleSanitizer.class) String title,
                                   @JsonDeserialize(using = XssSanitizer.class) String content,
                                   Long categoryId
) {
}
