package tf.tailfriend.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tf.tailfriend.notification.config.NotificationSender;
import tf.tailfriend.notification.entity.dto.NotificationDto;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationSender notificationSender;

    public void sendNotification(NotificationDto dto) {
        notificationSender.sendNotification(dto); // RabbitMQ로 전송
    }
}