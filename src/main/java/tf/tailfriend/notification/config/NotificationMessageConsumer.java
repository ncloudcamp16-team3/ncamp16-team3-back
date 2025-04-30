package tf.tailfriend.notification.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tf.tailfriend.notification.config.RabbitConfig;
import tf.tailfriend.notification.entity.Notification;
import tf.tailfriend.notification.entity.NotificationType;
import tf.tailfriend.notification.entity.dto.NotificationDto;
import tf.tailfriend.notification.repository.NotificationDao;
import tf.tailfriend.notification.repository.NotificationTypeDao;
import tf.tailfriend.notification.service.FirebaseService;
import tf.tailfriend.notification.service.NotificationService;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.repository.UserDao;


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationMessageConsumer {

    private final UserDao userDao;
    private final NotificationDao notificationDao;
    private final NotificationTypeDao notificationTypeDao;
    private final FirebaseService firebaseService; // FCM 발송용 서비스 주입
    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void receiveMessage(NotificationDto message) {

        try {
            User user = userDao.findById(message.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

            NotificationType notificationType = notificationTypeDao.findById(message.getNotifyTypeId())
                    .orElseThrow(() -> new IllegalArgumentException("알림 타입 없음"));

            Notification notification = Notification.builder()
                    .user(user)
                    .notificationType(notificationType)
                    .content(message.getContent())
                    .readStatus(false)
                    .build();
            notificationDao.save(notification);

            System.out.println("[RabbitMQ] Notification saved to DB, now sending FCM...");
            notificationService.sendNotificationToUser(message);

        } catch (Exception e) {
            log.error("[RabbitMQ] Error while processing message", e);
        }
    }
}
