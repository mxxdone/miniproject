package com.mxxdone.miniproject.dto.comment;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mxxdone.miniproject.util.XssSanitizer;

public record CommentUpdateRequestDto(@JsonDeserialize(using = XssSanitizer.class) String content) {
}