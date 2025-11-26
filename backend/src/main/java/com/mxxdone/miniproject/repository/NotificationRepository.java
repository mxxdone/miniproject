package com.mxxdone.miniproject.repository;

import com.mxxdone.miniproject.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 알림 목록 조회(최신순 정렬)
    List<Notification> findAllByReceiverIdOrderByCreatedAtDesc(Long targetUserId);

    // 읽지 않은 알림 개수 조회
    long countByReceiverIdAndIsReadFalse(Long receiverId);
}
