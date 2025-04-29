package tf.tailfriend.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.notification.config.FirebaseConfig;
import tf.tailfriend.notification.config.NotificationMessageProducer;
import tf.tailfriend.notification.entity.Notification;
import tf.tailfriend.notification.entity.NotificationType;
import tf.tailfriend.notification.entity.dto.NotificationDto;
import tf.tailfriend.notification.repository.NotificationDao;
import tf.tailfriend.notification.repository.NotificationTypeDao;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.repository.UserDao;


@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMessageProducer notificationMessageProducer; // 🔥 추가


    public void sendNotification(NotificationDto dto) {

        // Firebase 푸시 알림 전송
        notificationMessageProducer.sendNotification(dto);
    }

}