package tf.tailfriend.notification.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import tf.tailfriend.notification.entity.dto.NotificationDto;
import tf.tailfriend.notification.service.UserFcmService;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationMessageProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;  // ObjectMapper 주입 필요
    private final UserFcmService userFcmService;  // ✅ FCM 토큰 조회용 서비스 주입

    public void sendNotification(NotificationDto message) {
        try {
            // ✅ FCM 토큰 조회 후 DTO에 삽입
            userFcmService.findByUserId(message.getUserId()).ifPresent(userFcm -> {
                message.setFcmToken(userFcm.getFcmToken());
            });

            // 디버깅: 보낼 메시지를 JSON 문자열로 출력
            String jsonMessage = objectMapper.writeValueAsString(message);
            log.info("[RabbitMQ] Sending notification message: {}", jsonMessage);

            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE_NAME,
                    RabbitConfig.ROUTING_KEY,
                    message
            );

            log.info("[RabbitMQ] Notification message sent successfully.");

        } catch (JsonProcessingException e) {
            log.error("[RabbitMQ] Failed to serialize notification message", e);
        } catch (Exception e) {
            log.error("[RabbitMQ] Failed to send notification message", e);
        }
    }
}
