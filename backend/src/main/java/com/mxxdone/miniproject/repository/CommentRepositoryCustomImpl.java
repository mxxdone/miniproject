package com.mxxdone.miniproject.repository;

import com.mxxdone.miniproject.domain.Comment;
import com.mxxdone.miniproject.domain.QComment;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.mxxdone.miniproject.domain.QComment.comment;
import static com.mxxdone.miniproject.domain.QUser.user;

@RequiredArgsConstructor
public class CommentRepositoryCustomImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;


    @Override
    public List<Comment> findVisibleCommentsByPostId(Long postId) {
        QComment subComment = new QComment("subComment");

        return queryFactory
                .selectDistinct(comment)
                .from(comment)
                .leftJoin(comment.author, user).fetchJoin()
                .leftJoin(comment.children, subComment).fetchJoin()
                .where(
                        comment.post.id.eq(postId),
                        comment.parent.isNull(),
                        //삭제되지 않았거나, 삭제되지 않은 대댓글이 존재하는 댓글
                        comment.isDeleted.isFalse().or(
                                JPAExpressions.selectOne()
                                        .from(subComment)
                                        .where(subComment.parent.eq(comment), subComment.isDeleted.isFalse())
                                        .exists()
                        )
                )
                .orderBy(comment.createdAt.asc())
                .fetch();
    }
}
