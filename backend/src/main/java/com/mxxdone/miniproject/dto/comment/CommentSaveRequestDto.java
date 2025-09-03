package com.mxxdone.miniproject.dto.comment;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mxxdone.miniproject.config.XssSanitizer;

public record CommentSaveRequestDto(Long postId,
                                    @JsonDeserialize(using = XssSanitizer.class) String content,
                                    Long parentId) {
}