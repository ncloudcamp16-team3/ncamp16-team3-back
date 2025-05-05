package tf.tailfriend.notification.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static ch.qos.logback.core.testUtil.EnvUtilForTests.isLinux;
import static tf.tailfriend.notification.config.RabbitConfig.*;

@Configuration
@RequiredArgsConstructor
public class RabbitDynamicListenerConfig {

    private final ConnectionFactory connectionFactory;
    private final MessageConverter messageConverter;
    private final NotificationMessageConsumer consumer; // 메시지를 실제로 처리할 컴포넌트
    private final AmqpAdmin amqpAdmin;  // ← 추가된 의존성

    private boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    @Bean
    public SimpleMessageListenerContainer messageListenerContainer() {
        String queueName = isLinux() ? QUEUE_NAME : QUEUE_NAME_DEV;
        String exchangeName = isLinux() ? EXCHANGE_NAME : EXCHANGE_NAME_DEV;
        String routingKey = isLinux() ? ROUTING_KEY : ROUTING_KEY_DEV;

        // === 1. DLX 설정 ===
        DirectExchange dlxExchange = new DirectExchange(DLX_EXCHANGE_NAME);
        Queue dlxQueue = QueueBuilder.durable(DLX_QUEUE_NAME).build();
        Binding dlxBinding = BindingBuilder.bind(dlxQueue).to(dlxExchange).with(DLX_ROUTING_KEY);

        amqpAdmin.declareExchange(dlxExchange);
        amqpAdmin.declareQueue(dlxQueue);
        amqpAdmin.declareBinding(dlxBinding);

        // === 2. Retry Queue 설정 (재시도 후 메인 큐로 복귀) ===
        TopicExchange retryExchange = isLinux()
                ? new TopicExchange("notification.retry.exchange")
                : new TopicExchange("notification.retry.exchange.dev");
        Queue retryQueue = isLinux()
                ? QueueBuilder.durable("notification.retry.queue")
                .withArgument("x-dead-letter-exchange", exchangeName)  // DLX로 교환
                .withArgument("x-dead-letter-routing-key", routingKey)  // DLX 라우팅 키
                .withArgument("x-message-ttl", 5000)  // 메시지가 5초 후 TTL expired
                .build()
                : QueueBuilder.durable("notification.retry.queue.dev")
                .withArgument("x-dead-letter-exchange", exchangeName)  // DLX로 교환
                .withArgument("x-dead-letter-routing-key", routingKey)  // DLX 라우팅 키
                .withArgument("x-message-ttl", 5000)  // 메시지가 5초 후 TTL expired
                .build();
        Binding retryBinding = isLinux()
                ? BindingBuilder.bind(retryQueue).to(retryExchange).with("notification.retry.routing")
        : BindingBuilder.bind(retryQueue).to(retryExchange).with("notification.retry.routing.dev");

        amqpAdmin.declareExchange(retryExchange);
        amqpAdmin.declareQueue(retryQueue);
        amqpAdmin.declareBinding(retryBinding);

        // === 3. Main Queue 설정 (실패 시 DLX로 전송) ===
        TopicExchange mainExchange = new TopicExchange(exchangeName);
        Queue mainQueue = QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", DLX_ROUTING_KEY)
                .build();
        Binding mainBinding = BindingBuilder.bind(mainQueue).to(mainExchange).with(routingKey);

        amqpAdmin.declareExchange(mainExchange);
        amqpAdmin.declareQueue(mainQueue);
        amqpAdmin.declareBinding(mainBinding);

        // === 4. 리스너 컨테이너 ===
        MessageListenerAdapter adapter = new MessageListenerAdapter(consumer, messageConverter);
        adapter.setDefaultListenerMethod("receiveMessage");

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueues(mainQueue);  // 메인 큐만 리스너에 설정
        container.setMessageListener(adapter);
        container.setAcknowledgeMode(AcknowledgeMode.AUTO);
        container.setErrorHandler(new ConditionalRejectingErrorHandler(t -> {
            System.err.println("Error occurred, message will be dead-lettered.");
            t.printStackTrace(System.err);
            return true;
        }));

        return container;
    }
}
