package com.mxxdone.miniproject.repository;

import com.mxxdone.miniproject.domain.QCategory;
import com.mxxdone.miniproject.domain.QUser;
import com.mxxdone.miniproject.domain.Role;
import com.mxxdone.miniproject.dto.admin.AdminSummaryResponseDto;
import com.mxxdone.miniproject.dto.admin.DailyStatResponseDto;
import com.mxxdone.miniproject.dto.admin.PopularPostResponseDto;
import com.mxxdone.miniproject.dto.admin.RecentCommentResponseDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static com.mxxdone.miniproject.domain.QCategory.category;
import static com.mxxdone.miniproject.domain.QComment.comment;
import static com.mxxdone.miniproject.domain.QPost.post;
import static com.mxxdone.miniproject.domain.QPostLike.postLike;
import static com.mxxdone.miniproject.domain.QUser.user;

@Repository
@RequiredArgsConstructor
public class AdminStatsRepository {

    private final JPAQueryFactory queryFactory;

    // EC2(UTC) 환경에서 KST 기준으로 판단
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // 같은 category 테이블을 두 번 조인하기 때문에
    // 별칭을 다르게 해서 구분
    // static import된 category와 충돌 방지
    QCategory parentCategory = new QCategory("parentCategory");

    // KST -> Instant로 변환 (오늘 자정)
    private Instant startOfToday() {
        return LocalDate.now(KST).atStartOfDay(KST).toInstant();
    }

    // BooleanExpression — QueryDSL의 SQL 조건절 조각
    private BooleanExpression isNotAdminComment() {
        return comment.author.isNull()
                .or(comment.author.role.ne(Role.ADMIN));
    }

    public AdminSummaryResponseDto getSummary() {

        Instant todayStart = startOfToday();

        // 전체 게시글 수 구하기
        long totalPosts = Optional.ofNullable(
                queryFactory
                        .select(post.count())
                        .from(post)
                        .fetchOne() // 결과를 1개만 가져옴 (count라서)
        ).orElse(0L); //게시글이 없으면 null 대신 0으로 대체

        // 전체 유저 수 구하기
        long totalUsers = Optional.ofNullable(
                queryFactory.select(user.count())
                        .from(user)
                        .fetchOne()
        ).orElse(0L);

        // 전체 댓글 수 구하기
        // select count(id) from comment where is_deleted = false
        long totalComments = Optional.ofNullable(
                queryFactory.select(comment.count()).from(comment)
                        .leftJoin(comment.author, user)
                        .where(comment.isDeleted.isFalse(),
                                isNotAdminComment())
                        .fetchOne()
        ).orElse(0L);

        // 오늘 가입한 신규 유저 수
        // select count(id) from user where created_at >= 오늘 자정
        long todayNewUsers = Optional.ofNullable(
                queryFactory.select(user.count()).from(user)
                        .where(user.createdAt.goe(todayStart)) // goe: Greater or Equal
                        .fetchOne()
        ).orElse(0L);

        // 오늘 작성된 게시글 수
        long todayPosts = Optional.ofNullable(
                queryFactory.select(post.count()).from(post)
                        .where(post.createdAt.goe(todayStart))
                        .fetchOne()
        ).orElse(0L);

        // 오늘 작성된 댓글 수
        long todayComments = Optional.ofNullable(
                queryFactory.select(comment.count()).from(comment)
                        .where(comment.isDeleted.isFalse(),
                                comment.createdAt.goe(todayStart),
                                isNotAdminComment())
                        .fetchOne()
        ).orElse(0L);

        // DTO로 담기
        return new AdminSummaryResponseDto(
                totalPosts, totalUsers, totalComments,
                todayNewUsers, todayPosts, todayComments
        );
    }

    /**
     * 인기 게시글 (조회수 기준)
     * 리스트 목록과 함께 해당 게시글의 좋아요 수, 댓글 수도 서브쿼리로 묶어서 조회
     */
    public List<PopularPostResponseDto> getPopularPosts(int limit) {

        // 서브쿼리에서 user 겹쳐서 구분
        QUser commentAuthor = new QUser("commentAuthor");

        return queryFactory
                // Projections.constructor: DB에서 긁어온 데이터를 DTO(PopularPostResponseDto)의 생성자에 넣어줌
                .select(Projections.constructor(PopularPostResponseDto.class,
                        post.id,
                        post.title,
                        category.name, // 조인된 카테고리 테이블에서 이름 가져오기
                        parentCategory.slug,
                        category.slug,
                        post.viewCount.longValue(),

                        // 해당 게시글에 달린 좋아요 수
                        // SELECT COUNT(id) FROM post_like WHERE post_id = post.id
                        JPAExpressions.select(postLike.count())
                                .from(postLike)
                                .where(postLike.post.eq(post)), // eq: Equal (같다, ==)

                        // 이 게시글에 달린 댓글 수
                        JPAExpressions.select(comment.count())
                                .from(comment)
                                .leftJoin(comment.author, commentAuthor)
                                .where(
                                        comment.post.eq(post),
                                        comment.isDeleted.isFalse(),
                                        commentAuthor.isNull()
                                                .or(commentAuthor.role.ne(Role.ADMIN))
                                ),
                        post.createdAt
                ))
                .from(post)
                // 카테고리 정보를 가져오기 위해 카테고리 테이블과 LEFT JOIN
                .leftJoin(post.category, category)
                .leftJoin(category.parent, parentCategory)
                // 조회수(viewCount) 기준으로 내림차순(DESC) 정렬
                .orderBy(post.viewCount.desc())

                // 파라미터로 받은 갯수(limit)만큼만 자르기
                .limit(limit)

                // 결과가 여러 건(List)일 때는 fetchOne() 대신 fetch()를 사용
                .fetch();
    }

    /**
     * 최근 댓글 목록 (게시글 정보 포함)
     */
    public List<RecentCommentResponseDto> getRecentComments(int limit) {
        return queryFactory
                .select(Projections.constructor(RecentCommentResponseDto.class,
                        comment.id,
                        comment.content,
                        comment.authorNickname,
                        post.id,    // 댓글이 달린 원본 게시글의 ID
                        post.title, // 댓글이 달린 원본 게시글의 제목
                        parentCategory.slug,
                        category.slug,
                        comment.createdAt
                ))
                .from(comment)
                .leftJoin(comment.post, post)
                .leftJoin(post.category, category)
                .leftJoin(category.parent, parentCategory)
                .leftJoin(comment.author, user)
                // 삭제 안 된 댓글만 최신순(날짜 내림차순)으로 정렬
                .where(comment.isDeleted.isFalse(),
                        isNotAdminComment())
                .orderBy(comment.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    /**
     * 일별 댓글 수 추이
     * 날짜별로 그룹화(GROUP BY)해서 집계
     */
    public List<DailyStatResponseDto> getCommentsDailyStats(int days) {
        Instant startDate = LocalDate.now(KST)
                .minusDays(days)
                .atStartOfDay(KST)
                .toInstant();

        DateTemplate<LocalDate> dateExpr = Expressions.dateTemplate(
                LocalDate.class,
                "cast(function('timezone', 'Asia/Seoul', {0}) as date)",
                comment.createdAt
        );

        List<Tuple> results = queryFactory
                .select(
                        dateExpr,            // 날짜값 (e.g. 2025-12-25)
                        comment.count()      // 해당 일 댓글 수
                )
                .from(comment)
                .where(
                        comment.isDeleted.isFalse(),
                        comment.createdAt.goe(startDate), // 작성일이 'N일 전의 자정'보다 크거나 같은 조건
                        isNotAdminComment()
                )
                .groupBy(dateExpr)          // 위에서 만든 날짜를 기준으로 GROUP BY
                .orderBy(dateExpr.asc())    // 날짜 오름차순(과거->현재)으로 정렬
                .fetch();                   // 복수개라 fetch()

        return results.stream()
                .map(tuple -> new DailyStatResponseDto(
                        // dateExpr 대신 인덱스로 꺼내서 Object로 받고 toString() → parse()
                        LocalDate.parse(tuple.get(0, Object.class).toString()),
                        tuple.get(1, Long.class)
                ))
                .toList();
    }
}
