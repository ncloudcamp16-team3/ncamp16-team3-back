package tf.tailfriend.notification.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tf.tailfriend.notification.entity.Notification;
import tf.tailfriend.notification.entity.NotificationType;
import tf.tailfriend.notification.entity.dto.NotificationDto;
import tf.tailfriend.notification.repository.NotificationDao;
import tf.tailfriend.notification.repository.NotificationTypeDao;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.repository.UserDao;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificationReceiver {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserDao userDao;
    private final NotificationDao notificationDao;
    private final NotificationTypeDao notificationTypeDao;

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void receiveMessage(NotificationDto message) {

        User user = userDao.findById(message.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        NotificationType notificationType = notificationTypeDao.findById(message.getNotifyTypeId())
                .orElseThrow(() -> new IllegalArgumentException("알림 타입 없음"));

        Notification notification= Notification.builder()
                .user(user)
                .notificationType(notificationType)
                .content(message.getContent())
                .readStatus(false)
                .build();
        notificationDao.save(notification);

        // 4. WebSocket 전송
        messagingTemplate.convertAndSend(
                "/api/notification/" + user.getId(),
                message.getContent()
        );
    }
}
