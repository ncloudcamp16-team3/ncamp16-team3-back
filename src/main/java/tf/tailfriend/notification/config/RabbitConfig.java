package tf.tailfriend.notification.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static ch.qos.logback.core.testUtil.EnvUtilForTests.isLinux;


@Configuration
public class RabbitConfig {

    public static final String QUEUE_NAME_DEV = "tailfriends.notification.queue.dev";
    public static final String EXCHANGE_NAME_DEV = "tailfriends.notification.exchange.dev";
    public static final String ROUTING_KEY_DEV = "tailfriend.notification.routing.dev";

    public static final String QUEUE_NAME = "tailfriends.notification.queue";
    public static final String EXCHANGE_NAME = "tailfriends.notification.exchange";
    public static final String ROUTING_KEY = "tailfriends.notification.routing";

    // Dead Letter Exchange 관련 설정
    public static final String DLX_QUEUE_NAME = "tailfriends.notification.dlx.queue";
    public static final String DLX_EXCHANGE_NAME = "tailfriends.notification.dlx.exchange";
    public static final String DLX_ROUTING_KEY = "tailfriends.notification.dlx.routing";

    // Linux 환경 확인을 위한 수정된 메서드
    private boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    // DLX Exchange 설정
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE_NAME);
    }

    // DLX 큐 설정
    @Bean
    public Queue dlxQueue() {
        return new Queue(DLX_QUEUE_NAME);
    }

    // DLX와 큐를 바인딩
    @Bean
    public Binding dlxBinding() {
        return BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with(DLX_ROUTING_KEY);
    }

    @Bean
    public Queue queue() {
        String queueName = isLinux() ? QUEUE_NAME : QUEUE_NAME_DEV;

        // 큐에 DLX 설정 추가
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE_NAME)  // DLX Exchange 설정
                .withArgument("x-dead-letter-routing-key", DLX_ROUTING_KEY) // DLX Routing Key 설정
                .build();
    }
    @Bean
    public TopicExchange exchange() {
        String exchangeName = isLinux() ? EXCHANGE_NAME : EXCHANGE_NAME_DEV ;
        return new TopicExchange(exchangeName);
    }



    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        String routingKey = isLinux() ? ROUTING_KEY : ROUTING_KEY_DEV;
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter()); // ✅ 꼭 필요
        return template;
    }
}
