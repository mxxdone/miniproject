package com.mxxdone.miniproject.listener;

import com.mxxdone.miniproject.domain.Notification;
import com.mxxdone.miniproject.domain.User;
import com.mxxdone.miniproject.dto.event.NotificationEvent;
import com.mxxdone.miniproject.repository.NotificationRepository;
import com.mxxdone.miniproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Async("taskExecutor") // AsuncConfig에서 정의한 스레드 풀 사용
    @Transactional(propagation = Propagation.REQUIRES_NEW) // 새로운 트랜잭션 시작
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 원래 트랜잭션 커밋 후 실행
    public void handleNotificationEvent(NotificationEvent event) {
        try {
            log.info("알림 이벤트 수신 및 비동기 처리 시작: receiverId={}", event.receiverId());

            // 수신자 조회
            User receiver = userRepository.findById(event.receiverId())
                    .orElseThrow(() -> new IllegalArgumentException("수신자를 찾을 수 없습니다."));

            // 알림 엔티티 생성
            Notification notification = Notification.builder()
                    .receiver(receiver)
                    .content(event.content())
                    .url(event.url())
                    .notificationType(event.notificationType())
                    .build();

            // DB에 저장
            notificationRepository.save(notification);

            log.info("알림 저장 완료");

        } catch (Exception e) {
            // 비동기 로직이므로 예외가 발생해도 메인 트랜잭션(댓글 저장)에는 영향X
            log.error("알림 저장 중 오류가 발생했습니다", e);
        }
    }
}
