package com.mxxdone.miniproject.dto.comment;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mxxdone.miniproject.util.XssSanitizer;

public record CommentSaveRequestDto(Long postId,
                                    @JsonDeserialize(using = XssSanitizer.class) String content,
                                    Long parentId,
                                    String guestName,
                                    String guestPassword) {
}