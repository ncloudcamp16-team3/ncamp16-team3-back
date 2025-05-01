package tf.tailfriend.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tf.tailfriend.notification.config.NotificationMessageProducer;
import tf.tailfriend.notification.entity.UserFcm;
import tf.tailfriend.notification.entity.dto.NotificationDto;
import tf.tailfriend.notification.entity.dto.UserFcmDto;


@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMessageProducer notificationMessageProducer; // 🔥 추가
    private final UserFcmService userFcmService;


    public void sendNotification(NotificationDto dto) {

        notificationMessageProducer.sendNotification(dto);
    }

    // 특정 사용자에게 직접 푸시 전송
    public void sendNotificationToUser(Integer userId, String title, String body) {
        userFcmService.findByUserId(userId).ifPresentOrElse(
                userFcm -> {
                    String fcmToken = userFcm.getFcmToken();
                    try {
                        Message message = Message.builder()
                                .setToken(fcmToken)
                                .setNotification(Notification.builder()
                                        .setTitle(title)
                                        .setBody(body)
                                        .build())
                                .build();
                        FirebaseMessaging.getInstance().send(message);
                        System.out.println("푸시 전송 성공: " + userId);
                    } catch (Exception e) {
                        System.err.println("푸시 전송 실패: " + e.getMessage());
                        e.printStackTrace();
                    }
                },
                () -> {
                    System.out.println("FCM 토큰이 없는 사용자입니다: userId = " + userId);
                }
        );
    }
}