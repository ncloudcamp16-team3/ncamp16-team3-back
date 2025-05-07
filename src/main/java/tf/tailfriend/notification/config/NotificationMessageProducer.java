package tf.tailfriend.notification.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import tf.tailfriend.notification.entity.UserFcm;
import tf.tailfriend.notification.entity.dto.NotificationDto;
import tf.tailfriend.notification.repository.UserFcmDao;
import tf.tailfriend.notification.service.UserFcmService;

import java.util.List;

import static ch.qos.logback.core.testUtil.EnvUtilForTests.isLinux;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationMessageProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final UserFcmDao userFcmDao;

    private boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    public void sendNotification(NotificationDto message) {
        try {
            // ✅ 사용자 ID로 모든 FCM 토큰 조회
            List<UserFcm> userFcmList = userFcmDao.findAllByUserId(message.getUserId());

            if (userFcmList.isEmpty()) {
                log.warn("[RabbitMQ] FCM 토큰이 없습니다. userId: {}", message.getUserId());
                return;
            }

            for (UserFcm userFcm : userFcmList) {

                NotificationDto clonedMessage = NotificationDto.builder()
                        .userId(message.getUserId())
                        .notifyTypeId(message.getNotifyTypeId())
                        .content(message.getContent())
                        .message(message.getMessage())
                        .senderId(message.getSenderId())
                        .fcmToken(userFcm.getFcmToken())
                        .messageId(message.getMessageId())
                        .build();

                // 디버깅: 보낼 메시지를 JSON 문자열로 출력
                String jsonMessage = objectMapper.writeValueAsString(clonedMessage);
                log.info("[RabbitMQ] Sending notification message: {}", jsonMessage);

                // 메시지 큐에 전송
                // 환경에 따른 큐 선택
                if (isLinux()) {
                    // 리눅스 환경일 때
                    rabbitTemplate.convertAndSend(
                            RabbitConfig.EXCHANGE_NAME,
                            RabbitConfig.ROUTING_KEY,
                            clonedMessage
                    );
                } else {
                    // 다른 환경일 때 (예: 개발 환경)
                    rabbitTemplate.convertAndSend(
                            RabbitConfig.EXCHANGE_NAME_DEV,
                            RabbitConfig.ROUTING_KEY_DEV,
                            clonedMessage
                    );
                }
            }

            log.info("[RabbitMQ] Notification messages sent successfully.");

        } catch (JsonProcessingException e) {
            log.error("[RabbitMQ] Failed to serialize notification message", e);
        } catch (Exception e) {
            log.error("[RabbitMQ] Failed to send notification message", e);
        }
    }
}
