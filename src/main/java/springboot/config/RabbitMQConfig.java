package springboot.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import springboot.Receiver;

/**
 * 角色表Service
 *
 * @author 祁军
 */
@Configuration
public class RabbitMQConfig {
    @Autowired
    private PropertiesConfig propertiesConfig;

    public static final String QUEUE_NAME = "first_queue";
    public static final String  ROUTER_KEY_1 = "*.orange.*";
    public static final String  ROUTER_KEY_2 = "*.apple.*";
    public static final String QUEUE_EXCHANGE_NAME = "first_exchange";


    @Bean
    @Primary
    public CachingConnectionFactory rabbitConnectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setHost("localhost");
        cachingConnectionFactory.setPort(5672);
        cachingConnectionFactory.setUsername("guest");
        cachingConnectionFactory.setPassword("guest");
        cachingConnectionFactory.setVirtualHost("/");
        return cachingConnectionFactory;
    }
    @Bean
    public RabbitAdmin rabbitAdmin() {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(rabbitConnectionFactory());
        TopicExchange topicExchange =(TopicExchange)ExchangeBuilder.topicExchange(QUEUE_EXCHANGE_NAME).durable(true).build();
        rabbitAdmin.declareExchange(topicExchange);
        Queue firstQueue = new Queue(QUEUE_NAME);
        rabbitAdmin.declareQueue(firstQueue);
        rabbitAdmin.declareBinding(BindingBuilder.bind(firstQueue).to(topicExchange).with(ROUTER_KEY_1));
        return rabbitAdmin;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(rabbitConnectionFactory());
    }


    @Bean
    MessageListenerAdapter listenerAdapter(Receiver receiver) {
        return new MessageListenerAdapter(receiver, "receive2");
    }

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory, MessageListenerAdapter messageListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(rabbitConnectionFactory());
        container.setQueueNames(QUEUE_NAME);
        container.setMessageListener(messageListenerAdapter);
        return container;
    }


}

