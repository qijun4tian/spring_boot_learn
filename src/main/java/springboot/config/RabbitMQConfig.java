package springboot.config;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import springboot.Receiver;

/**
 * 角色表Service
 *
 * @author 祁军
 */
@Configuration
@EnableConfigurationProperties(RabbitMqProperties.class)
@Slf4j
public class RabbitMQConfig {
    @Autowired
    private RabbitMqProperties rabbitMqProperties;

    public static final String QUEUE_NAME = "first_queue";
    public static final String ROUTER_KEY_1 = "*.orange.*";
    public static final String ROUTER_KEY_2 = "*.apple.*";
    public static final String QUEUE_EXCHANGE_NAME = "first_exchange";


    @Bean
    public CachingConnectionFactory rabbitConnectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setHost(rabbitMqProperties.getHost());
        cachingConnectionFactory.setPort(rabbitMqProperties.getPort());
        cachingConnectionFactory.setUsername(rabbitMqProperties.getUsername());
        cachingConnectionFactory.setPassword(rabbitMqProperties.getPassword());
        cachingConnectionFactory.setVirtualHost(rabbitMqProperties.getVirtualhost());
        cachingConnectionFactory.setPublisherConfirms(true);
        return cachingConnectionFactory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin() {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(rabbitConnectionFactory());
        TopicExchange topicExchange = (TopicExchange) ExchangeBuilder.topicExchange(QUEUE_EXCHANGE_NAME).durable(true).build();
        rabbitAdmin.declareExchange(topicExchange);
        Queue firstQueue = new Queue(QUEUE_NAME);
        rabbitAdmin.declareQueue(firstQueue);
        rabbitAdmin.declareBinding(BindingBuilder.bind(firstQueue).to(topicExchange).with(ROUTER_KEY_1));
        return rabbitAdmin;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(rabbitConnectionFactory());
    }

    // 方式1
    @Bean
    MessageListenerAdapter listenerAdapter(Receiver receiver) {
        return new MessageListenerAdapter(receiver, "receive2");
    }

    @Bean
    SimpleMessageListenerContainer container() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(rabbitConnectionFactory());
        container.setQueueNames(QUEUE_NAME);
        container.setPrefetchCount(1);
        container.setMaxConcurrentConsumers(1);
        container.setConcurrentConsumers(1);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener(new ChannelAwareMessageListener() {

            @Override
            public void onMessage(Message message, Channel channel) throws Exception {
                byte[] body = message.getBody();
                try {
                    log.info("receive msg: " + new String(body));
                    Thread.sleep(10000);
                    //do something
                } catch (Exception e) {
                } finally {
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false); //确认消息成功消费
                }

            }

        });
        return container;
    }


// 方式2
//    @Bean
//    public SimpleRabbitListenerContainerFactory myContainerFactory(
//            SimpleRabbitListenerContainerFactoryConfigurer configurer,
//            ConnectionFactory connectionFactory) {
//        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
//
//        factory.setPrefetchCount(1);
//        factory.setMaxConcurrentConsumers(1);
//        factory.setConcurrentConsumers(1);
//        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
//
//
//        configurer.configure(factory, connectionFactory);
//        return factory;
//    }


}

