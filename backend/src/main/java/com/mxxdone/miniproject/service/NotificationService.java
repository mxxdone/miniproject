package com.mxxdone.miniproject.service;

import com.mxxdone.miniproject.domain.Notification;
import com.mxxdone.miniproject.domain.User;
import com.mxxdone.miniproject.dto.notification.NotificationResponseDto;
import com.mxxdone.miniproject.repository.NotificationRepository;
import com.mxxdone.miniproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // 내 알림 목록 조회
    public List<NotificationResponseDto> findAllByUsername(String username) {
        User user = getUser(username);
        return notificationRepository.findAllByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(NotificationResponseDto::from)
                .toList();
    }

    // 안 읽은 알림 개수 조회
    public long countUnreadNotifications(String username) {
        User user = getUser(username);
        return notificationRepository.countByReceiverIdAndIsReadFalse(user.getId());
    }

    // 읽음 처리
    @Transactional
    public void markAsRead(Long notificationId, String username) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 알림입니다."));

        // 본인 알림인지 확인
        if (!notification.getReceiver().getUsername().equals(username)) {
            throw new AccessDeniedException("해당 알림을 읽을 권한이 없습니다.");
        }
        // isRead 값을 true로 변경 (더티 체킹으로 자동 저장)
        notification.read();
    }

    // 전체 읽음 처리 (모두 읽음 버튼용)
    @Transactional
    public void markAllAsRead(String username) {
        User user = getUser(username);
        List<Notification> notifications = notificationRepository.findAllByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
        notifications.forEach(Notification::read);
    }

    // 사용자 조회
    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
    }
}
