package com.mxxdone.miniproject.repository;

import com.mxxdone.miniproject.dto.post.PostSummaryResponseDto;
import com.mxxdone.miniproject.dto.post.QPostSummaryResponseDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.mxxdone.miniproject.domain.QCategory.category;
import static com.mxxdone.miniproject.domain.QComment.comment;
import static com.mxxdone.miniproject.domain.QPost.post;
import static com.mxxdone.miniproject.domain.QUser.user;

@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PostSummaryResponseDto> findPostsWithConditions(List<Long> categoryIds, String searchType, String keyword, Pageable pageable) {
        List<PostSummaryResponseDto> content = queryFactory
                .select(new QPostSummaryResponseDto(
                        post.id,
                        post.title,
                        post.content,
                        category.name,
                        user.username,
                        user.nickname,
                        new CaseBuilder()
                                .when(comment.isDeleted.isFalse())
                                .then(1L)
                                .otherwise(0L)
                                .sum(), // 삭제되지 않은 댓글만 count 
                        post.createdAt
                ))
                .from(post)
                .leftJoin(post.author, user)
                .leftJoin(post.category, category)
                .leftJoin(post.comments, comment)
                .where(
                        categoryIn(categoryIds),
                        searchEq(searchType, keyword)
                )
                .groupBy(post.id, category.name, user.username, user.nickname)
                .orderBy(post.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 페이징을 위한 post total 계산
        Long total = queryFactory
                .select(post.count())
                .from(post)
                .where(
                        categoryIn(categoryIds),
                        searchEq(searchType, keyword)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression categoryIn(List<Long> categoryIds) {
        return categoryIds != null && !categoryIds.isEmpty() ? post.category.id.in(categoryIds) : null;
    }

    private BooleanExpression searchEq(String searchType, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return null;
        }
        if ("title".equals(searchType)) {
            return post.title.containsIgnoreCase(keyword);
        } else if ("content".equals(searchType)) {
            return post.content.containsIgnoreCase(keyword);
        }
        return post.title.containsIgnoreCase(keyword).or(post.content.containsIgnoreCase(keyword));
    }
}
