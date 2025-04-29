package tf.tailfriend.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tf.tailfriend.notification.config.NotificationMessageProducer;
import tf.tailfriend.notification.entity.dto.NotificationDto;


@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMessageProducer notificationMessageProducer; // 🔥 추가
    private final UserFcmService userFcmService;


    public void sendNotification(NotificationDto dto) {

        notificationMessageProducer.sendNotification(dto);
    }

    public void sendNotificationToUser(Integer userId, String title, String body) {
        userFcmService.getFcmTokenByUserId(userId).ifPresentOrElse(
                fcmToken -> {
                    try {
                        Message message = Message.builder()
                                .setToken(fcmToken)
                                .setNotification(Notification.builder()
                                        .setTitle(title)
                                        .setBody(body)
                                        .build())
                                .build();
                        FirebaseMessaging.getInstance().send(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                () -> {
                    System.out.println("FCM 토큰이 없는 사용자입니다: userId = " + userId);

                }
        );
    }


}