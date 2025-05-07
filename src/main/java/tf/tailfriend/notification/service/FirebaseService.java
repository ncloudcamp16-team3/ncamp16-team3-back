package tf.tailfriend.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseService {

//    public void sendPushNotification(NotificationDto notificationDto) {
//        try {
//            // 디버깅: 보내려는 데이터 출력
//            log.info("[Firebase] Preparing push notification. Target Token: {}, Content: {}",
//                    notificationDto.getFcmToken(), notificationDto.getContent());
//
//            Message message = Message.builder()
//                    .setToken(notificationDto.getFcmToken()) // fcmToken 사용
//                    .setNotification(Notification.builder()
//                            .setTitle("알림")
//                            .setBody(notificationDto.getContent())
//                            .build())
//                    .build();
//
//            String response = FirebaseMessaging.getInstance().send(message);
//
//            // 성공했을 때 로그
//            log.info("[Firebase] Successfully sent push notification. Response: {}", response);
//
//        } catch (Exception e) {
//            // 실패했을 때 에러 로그
//            log.error("[Firebase] Failed to send push notification", e);
//        }
//    }
}
