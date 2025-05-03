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

import java.util.HashSet;
import java.util.Set;


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationMessageConsumer {

    private final UserDao userDao;
    private final NotificationDao notificationDao;
    private final NotificationTypeDao notificationTypeDao;
    private final NotificationService notificationService;



    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void receiveMessage(NotificationDto message) {


        String messageId = message.getMessageId();

        if (notificationDao.existsByMessageId(messageId)) {
            log.info("이미 처리된 메시지 ID입니다. 수신을 건너뜁니다. 메시지 ID: {}", messageId);
            return;  // 중복 메시지라면 전송하지 않음
        }

        try {
            User user = userDao.findById(message.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

            NotificationType notificationType = notificationTypeDao.findById(message.getNotifyTypeId())
                    .orElseThrow(() -> new IllegalArgumentException("알림 타입 없음"));

            // 2. notifyTypeId == 5일 경우 content 중복 확인
            if (message.getNotifyTypeId() != null && message.getNotifyTypeId()==5) {
                System.out.println("중복 체크 시작 - userId: " + user.getId() +
                        ", notifyTypeId: 5" +
                        ", content: " + message.getContent());

                Notification existingNotification =
                        notificationDao.findFirstByUserAndNotificationTypeIdAndContent(user, 5, message.getContent());

                if (existingNotification != null) {
                    System.out.println("기존 알림이 존재합니다. readStatus를 true로 업데이트합니다.");
                    // 빌더를 사용하여 readStatus 값을 false로 설정
                    Notification updated = existingNotification.toBuilder()
                            .readStatus(false)
                            .build();
                    notificationDao.save(updated);
                    notificationService.sendNotificationToUser(message);
                    return;
                }
            }


            Notification notification = Notification.builder()
                    .user(user)
                    .notificationType(notificationType)
                    .content(message.getContent())
                    .readStatus(false)
                    .messageId(messageId)
                    .build();
            notificationDao.save(notification);

            System.out.println("[RabbitMQ] Notification saved to DB, now sending FCM...");
            notificationService.sendNotificationToUser(message);

        } catch (Exception e) {
            log.error("[RabbitMQ] Error while processing message", e);
        }
    }
}
