package tf.tailfriend.notification.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import tf.tailfriend.notification.entity.dto.NotificationDto;

import static tf.tailfriend.notification.config.RabbitConfig.DLX_QUEUE_NAME;

@Configuration
public class DlxQueueConsumerConfig {

    private final RabbitTemplate rabbitTemplate;

    private boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    public DlxQueueConsumerConfig(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // DLX 큐에서 메시지를 소비하여 재시도 큐로 전송하는 리스너
    public void handleDlxQueueMessage(NotificationDto notificationDto) {
        try {
            System.out.println("DLX 큐에서 받은 메시지: " + notificationDto);

            // 리눅스 환경과 개발 환경에 따라 다른 교환기와 라우팅 키 사용
            String exchange = isLinux() ? "notification.retry.exchange" : "notification.retry.exchange.dev";
            String routingKey = isLinux() ? "notification.retry.routing" : "notification.retry.routing.dev";

            // 재시도 큐로 메시지 전송
            rabbitTemplate.convertAndSend(exchange, routingKey, notificationDto);
            System.out.println("재시도 큐로 메시지를 전송했습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            // 예외 처리 로직 (예: 로그 기록)
        }
    }

    @Bean
    public SimpleMessageListenerContainer simpleMessageListenerContainer() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(rabbitTemplate.getConnectionFactory());

        // DLX 큐에 대한 설정
        container.setQueues(new Queue(DLX_QUEUE_NAME));  // DLX 큐 설정

        // 메시지 리스너 설정
        MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(this, "handleDlxQueueMessage");


        Jackson2JsonMessageConverter jsonMessageConverter = new Jackson2JsonMessageConverter();
        listenerAdapter.setMessageConverter(jsonMessageConverter);

        container.setMessageListener(listenerAdapter);  // 메시지 리스너 어댑터를 설정

        // 설정: Acknowledge 모드 등 (옵션)
        container.setAcknowledgeMode(AcknowledgeMode.AUTO); // 자동 ACK 설정

        return container;
    }
}