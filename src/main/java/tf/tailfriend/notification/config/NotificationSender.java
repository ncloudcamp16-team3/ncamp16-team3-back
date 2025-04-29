package tf.tailfriend.notification.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import tf.tailfriend.notification.entity.dto.NotificationDto;

@Service
@RequiredArgsConstructor
public class NotificationSender {


    private final RabbitTemplate rabbitTemplate;

    public void sendNotification(NotificationDto message) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, RabbitConfig.ROUTING_KEY, message);

    }

}