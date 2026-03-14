package com.mxxdone.miniproject.service;

import com.mxxdone.miniproject.dto.admin.AdminSummaryResponseDto;
import com.mxxdone.miniproject.dto.admin.DailyStatResponseDto;
import com.mxxdone.miniproject.dto.admin.PopularPostResponseDto;
import com.mxxdone.miniproject.dto.admin.RecentCommentResponseDto;
import com.mxxdone.miniproject.repository.AdminStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatsService {

    private final AdminStatsRepository adminStatsRepository;

    private static final int MIN_LIMIT = 1;
    private static final int MAX_POPULAR_POSTS = 20;
    private static final int MAX_RECENT_COMMENTS = 50;
    private static final int MAX_DAILY_STATS_DAYS = 365;

    public AdminSummaryResponseDto getSummary() {
        return adminStatsRepository.getSummary();
    }

    public List<PopularPostResponseDto> getPopularPosts(int limit) {
        int validLimit = Math.min(Math.max(limit, MIN_LIMIT), MAX_POPULAR_POSTS);
        return adminStatsRepository.getPopularPosts(validLimit);
    }

    public List<RecentCommentResponseDto> getRecentComments(int limit) {
        int validLimit = Math.min(Math.max(limit, MIN_LIMIT), MAX_RECENT_COMMENTS);
        return adminStatsRepository.getRecentComments(validLimit);
    }

    public List<DailyStatResponseDto> getCommentsDailyStats(int days) {
        int validDays = Math.min(Math.max(days, MIN_LIMIT), MAX_DAILY_STATS_DAYS);
        return adminStatsRepository.getCommentsDailyStats(validDays);
    }
}
