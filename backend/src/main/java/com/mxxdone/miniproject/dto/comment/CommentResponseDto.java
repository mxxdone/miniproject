package com.mxxdone.miniproject.dto.comment;

import com.mxxdone.miniproject.domain.Comment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "댓글 응답 DTO")
public record CommentResponseDto(
        @Schema(description = "댓글 ID", example = "10")
        Long id,

        @Schema(description = "댓글 내용 (삭제된 경우 메시지 대체됨)", example = "안녕하세요 테스트 댓글입니다.")
        String content,

        @Schema(description = "작성자 아이디 (비회원은 null)", example = "user123")
        String authorUsername,

        @Schema(description = "작성자 닉네임", example = "닉네임123")
        String authorNickname,

        @Schema(description = "비회원 작성 여부", example = "true")
        boolean isGuest,

        @Schema(description = "삭제 여부", example = "false")
        boolean isDeleted,

        @Schema(description = "대댓글 목록")
        List<CommentResponseDto> children,

        @Schema(description = "작성 일시")
        Instant createdAt,

        @Schema(description = "수정 일시")
        Instant updatedAt
) {
    public static CommentResponseDto from(Comment comment) {
        // 댓글 로직: 작성자 닉네임 결정 (스냅샷 우선 -> 비회원 이름 -> 알수없음)
        String displayNickname = comment.getAuthorNickname();
        String displayUsername = comment.getAuthorUsername();

        if (displayNickname == null) {
            displayNickname = comment.getGuestName();
        }

        if (displayNickname == null) {
            displayNickname = "(알수없음)";
        }

        return new CommentResponseDto(
                comment.getId(),
                comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent(),
                displayUsername, // 스냅샷 아이디 (비회원은 null)
                displayNickname,
                comment.getAuthorUsername() == null, // 작성자가 없으면 true
                comment.isDeleted(),
                comment.getChildren().stream()
                        .map(CommentResponseDto::from)
                        .toList(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}