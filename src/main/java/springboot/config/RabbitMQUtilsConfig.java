package springboot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * @author 祁军
 */
//@Slf4j
//@Configuration
//public class RabbitMQUtilsConfig {
//
//    @Bean
//    public CachingConnectionFactory rabbitMQUtilsConnectionFactory() {
//        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
//        cachingConnectionFactory.setHost("localhost");
//        cachingConnectionFactory.setPort(5672);
//        cachingConnectionFactory.setUsername("guest");
//        cachingConnectionFactory.setPassword("guest");
//        cachingConnectionFactory.setVirtualHost("/");
//        cachingConnectionFactory.setPublisherConfirms(true);
//        return cachingConnectionFactory;
//    }
//
//    @Bean
//    @Qualifier("utilsRabbitTemplate")
//    public RabbitTemplate utilsRabbitTemplate() {
//        RabbitTemplate rabbitTemplate = new RabbitTemplate(rabbitMQUtilsConnectionFactory());
//        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
//            if (!ack) {
//                log.info("sender not send message to the right exchange" + " correlationData=" + correlationData + " ack=" + ack + " cause" + cause);
//            } else {
//                log.info("send message to the right exchange by utils" + " correlationData=" + correlationData + " ack=" + ack + " cause" + cause);
//            }
//        });
//        //消息是否到达正确的消息队列，如果没有会把消息返回
//        rabbitTemplate.setReturnCallback((message, replyCode, replyText, tmpExchange, tmpRoutingKey) -> {
//            log.info("Sender send message failed: " + message + " " + replyCode + " " + replyText + " " + tmpExchange + " " + tmpRoutingKey);
//            //try to resend msg
//        });
//
//        RetryTemplate retryTemplate = new RetryTemplate();
//        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
//        backOffPolicy.setInitialInterval(500);
//        backOffPolicy.setMultiplier(10.0);
//        backOffPolicy.setMaxInterval(10000);
//        retryTemplate.setBackOffPolicy(backOffPolicy);
//        rabbitTemplate.setRetryTemplate(retryTemplate);
//        rabbitTemplate.setMandatory(true);
//        return rabbitTemplate;
//    }
//}
