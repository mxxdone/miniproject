package com.mxxdone.miniproject.repository;

import com.mxxdone.miniproject.domain.QCategory;
import com.mxxdone.miniproject.dto.post.PostSummaryResponseDto;
import com.mxxdone.miniproject.dto.post.QPostSummaryResponseDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

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

        // 셀프 조인(Self-Join)을 위한 Q-Type 별칭(Alias) 생성
        // category 테이블은 parent_id로 자기 자신을 참조하는 계층 구조
        // 게시글의 '자식 카테고리'와 '부모 카테고리' 정보를 한 번에 가져오기 위해
        // category 테이블을 두 개인 것처럼 사용해야함
        // 'parentCategory'는 부모 카테고리 정보를 담기 위한 가상 테이블
        QCategory parentCategory = new QCategory("parentCategory");

        List<PostSummaryResponseDto> content = queryFactory
                .select(new QPostSummaryResponseDto(
                        post.id,
                        post.title,
                        post.content,
                        category.name,
                        user.username,
                        user.nickname,
                        // 스칼라 서브쿼리(Scalar Subquery)를 이용한 댓글 수 계산
                        JPAExpressions
                                .select(comment.count())
                                .from(comment)
                                .where(
                                        comment.post.eq(post), // 현재 게시글(post)에 속한 댓글만
                                        comment.isDeleted.isFalse() // 삭제되지 않은 댓글만
                                ),
                        post.createdAt,
                        new CaseBuilder()
                                .when(category.parent.isNotNull()).then(parentCategory.slug) // 부모가 있으면 부모 slug
                                .otherwise(category.slug) // 부모가 없으면 자신의 slug
                                .as("parentSlug"),
                        new CaseBuilder()
                                .when(category.parent.isNotNull()).then(category.slug) // 부모가 있으면 자신의 slug
                                .otherwise((String) null) // 부모가 없으면 null
                                .as("childSlug")
                ))
                .from(post)
                .leftJoin(post.author, user)
                .leftJoin(post.category, category)
                // 부모 카테고리 정보를 위한 JOIN
                // 자식 카테고리(category)의 parent 필드를 이용해 부모 카테고리 별칭(parentCategory)과 연결
                // ON 절을 명시적으로 사용하여 셀프 조인 관계를 정확히 지정
                .leftJoin(category.parent, parentCategory)
                .where(
                        categoryIn(categoryIds),
                        searchEq(searchType, keyword)
                )
                .orderBy(post.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 페이징 성능 최적화를 위한 count 쿼리 분리
        // PageableExecutionUtils.getPage()는 content.size()와 pageable.getPageSize()를 비교하여,
        // 마지막 페이지일 경우 불필요한 count 쿼리를 실행하지 않도록 최적화
        var countQuery = queryFactory
                .select(post.count())
                .from(post)
                .where(
                        categoryIn(categoryIds),
                        searchEq(searchType, keyword)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
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