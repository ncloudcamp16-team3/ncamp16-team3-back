package tf.tailfriend.notification.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import tf.tailfriend.notification.entity.dto.NotificationDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationMessageProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;  // ObjectMapper 주입 필요

    public void sendNotification(NotificationDto message) {
        try {
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
