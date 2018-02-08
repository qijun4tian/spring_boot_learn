package springboot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import springboot.Performance.Constant;
import springboot.Performance.PerformanceTestConfirmProducer;
import springboot.utils.MessageFatalExceptionStrategy;

import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 祁军
 */

@Configuration
//@EnableConfigurationProperties(RabbitMqProperties.class)
@Slf4j
public class RabbitMQTestConfig {
    public static AtomicInteger count = new AtomicInteger(0);
    @Bean
    @Primary
    @Qualifier("confirmConnectionFactory")
    public CachingConnectionFactory confirmConnectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        //        cachingConnectionFactory.setHost("192.168.1.18");
        cachingConnectionFactory.setHost("localhost");
        cachingConnectionFactory.setPort(5672);
//        cachingConnectionFactory.setUsername("fmis");
//        cachingConnectionFactory.setPassword("123456");
        cachingConnectionFactory.setUsername("guest");
        cachingConnectionFactory.setPassword("guest");
        cachingConnectionFactory.setPublisherConfirms(true);
        cachingConnectionFactory.setPublisherReturns(true);
        return cachingConnectionFactory;
    }
    @Bean
    @Qualifier("transactedConnectionFactory")
    public CachingConnectionFactory transactedConnectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
//        cachingConnectionFactory.setHost("192.168.1.18");
        cachingConnectionFactory.setHost("localhost");
        cachingConnectionFactory.setPort(5672);
//        cachingConnectionFactory.setUsername("fmis");
//        cachingConnectionFactory.setPassword("123456");
        cachingConnectionFactory.setUsername("guest");
        cachingConnectionFactory.setPassword("guest");
        cachingConnectionFactory.setVirtualHost("/");
        cachingConnectionFactory.setPublisherReturns(true);
        return cachingConnectionFactory;
    }

    @Bean
    public RabbitAdmin TestAdmin() {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(transactedConnectionFactory());
        DirectExchange topicExchange = (DirectExchange) ExchangeBuilder.directExchange("direct.excharge").durable(true).build();
        Queue confrimQueue = new Queue("test.comfirm",true,false,false);
        Queue transactedQueue = new Queue("test.transacted",true,false,false);
        rabbitAdmin.declareExchange(topicExchange);
        rabbitAdmin.declareQueue(confrimQueue);
        rabbitAdmin.declareQueue(transactedQueue);
        rabbitAdmin.declareBinding(BindingBuilder.bind(confrimQueue).to(topicExchange).withQueueName());
        rabbitAdmin.declareBinding(BindingBuilder.bind(transactedQueue).to(topicExchange).withQueueName());
        return rabbitAdmin;
    }


    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Qualifier("comfirmRabbitTemplate")
    public RabbitTemplate comfirmRabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(confirmConnectionFactory());
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
                    if (!ack) {
                        log.info("confirm sender not send message to the right exchange" + " correlationData=" + correlationData + " ack=" + ack + " cause" + cause);
                    } else {
//                log.info("confirm sender send message to the right exchange" + " correlationData=" + correlationData + " ack=" + ack + " cause" + cause);
                        if (System.currentTimeMillis() < PerformanceTestConfirmProducer.startTime +  Constant.oneSecond) {
                            count.incrementAndGet();
//                            log.info("get the count "+i);
                        }
                    }
                }
        );
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, tmpExchange, tmpRoutingKey) -> {
            System.out.println(message);
            log.info("confirm Sender send message failed: " + message + " " + replyCode + " " + replyText + " " + tmpExchange + " " + tmpRoutingKey);
            //try to resend msg
        });
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Qualifier("transactedRabbitTemplate")
    public RabbitTemplate transactedRabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(transactedConnectionFactory());
        rabbitTemplate.setChannelTransacted(true);
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, tmpExchange, tmpRoutingKey) -> {
            System.out.println(message);
            log.info("transacted Sender send message failed: " + message + " " + replyCode + " " + replyText + " " + tmpExchange + " " + tmpRoutingKey);
            //try to resend msg
        });
        return rabbitTemplate;

    }

    @Bean
    public SimpleRabbitListenerContainerFactory myContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        factory.setPrefetchCount(1);
        factory.setMaxConcurrentConsumers(1);
        factory.setConcurrentConsumers(1);
//        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setDefaultRequeueRejected(true);
        factory.setErrorHandler(new ConditionalRejectingErrorHandler(new MessageFatalExceptionStrategy()));


        configurer.configure(factory, transactedConnectionFactory());
        return factory;
    }


    @Bean
    public SimpleRabbitListenerContainerFactory myContainerFactory1(
            SimpleRabbitListenerContainerFactoryConfigurer configurer
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        factory.setPrefetchCount(1);
        factory.setMaxConcurrentConsumers(1);
        factory.setConcurrentConsumers(1);
//        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setDefaultRequeueRejected(true);
        factory.setErrorHandler(new ConditionalRejectingErrorHandler(new MessageFatalExceptionStrategy()));


        configurer.configure(factory, confirmConnectionFactory());
        return factory;
    }


}
